package edu.iis.mto.testreactor.washingmachine;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {

    @Mock
    private DirtDetector dirtDetector;
    @Mock
    private Engine engine;
    @Mock
    private WaterPump waterPump;
    private WashingMachine washingMashine;
    Material unrelevant;
    double properWeightKg;
    LaundryBatch properLaundry;
    Program staticProgram;
    Program autoDetectProgram;
    ProgramConfiguration programConfiguration;
    ProgramConfiguration autoDetectedprogramConfiguration;
    Random random;
    @BeforeEach
    void setUp() throws Exception {
        random=new Random();
        unrelevant =Material.COTTON;
        properWeightKg=7d;
        staticProgram = Program.LONG;
        autoDetectProgram=Program.AUTODETECT;
        properLaundry= batch(unrelevant, properWeightKg);
        programConfiguration= staticProgramWithSpin(staticProgram);
        autoDetectedprogramConfiguration = autoDetectProgramWithSpin(autoDetectProgram);
        washingMashine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void properBatchWithStaticProgram() {
        LaundryStatus result = washingMashine.start(properLaundry,programConfiguration);
        assertEquals(success(staticProgram), result);
    }

    @Test
    void overWeightBatchWithJeansOrWoolMaterialWithNullProgram() {
        double overWeightKG=5d;
        Material []materials={Material.JEANS,Material.WOOL};
        LaundryBatch overWeightLaundry=batch(materials[random.nextInt(materials.length)], overWeightKG);
        LaundryStatus result = washingMashine.start(overWeightLaundry,null);
        assertEquals(overWeight(), result);
    }

    @Test
    void overWeightBatchWithoutJeansOrWoolMaterialJeWithNullProgram() {
        double overWeightKG=12d;
        Material []materials={Material.SYNTETIC,Material.DELICATE,Material.COTTON};
        LaundryBatch overWeightLaundry=batch(materials[random.nextInt(materials.length)], overWeightKG);
        LaundryStatus result = washingMashine.start(overWeightLaundry,null);
        assertEquals(overWeight(), result);
    }

    @Test
    void properBatchWithStaticProgramShouldThrowWaterPumpExceptionInPourMethod() {
        WaterPump brokenWaterPump=new WaterPump() {
            @Override
            public void pour(double weigth) throws WaterPumpException {
                throw new WaterPumpException();
            }
            @Override
            public void release() throws WaterPumpException {}
        };
        washingMashine=new WashingMachine(dirtDetector, engine, brokenWaterPump);
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(waterPumpFailure(), result);
    }

    @Test
    void properBatchWithStaticProgramShouldThrowWaterPumpExceptionInReleaseMethod() {
        WaterPump brokenWaterPump=new WaterPump() {
            @Override
            public void pour(double weigth) throws WaterPumpException {
            }
            @Override
            public void release() throws WaterPumpException {
                throw new WaterPumpException();
            }
        };
        washingMashine=new WashingMachine(dirtDetector, engine, brokenWaterPump);
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(waterPumpFailure(), result);
    }



    @Test
    void properBatchWithStaticProgramShouldThrowEngineExceptionInRunWashingMethod() {
       Engine brokenEngine = new Engine() {
           @Override
           public void runWashing(int timeInMinutes) throws EngineException {
               throw new EngineException();
           }
           @Override
           public void spin() throws EngineException {
           }
       };
        washingMashine=new WashingMachine(dirtDetector, brokenEngine, waterPump);
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(engineFailue(), result);
    }


    @Test
    void properBatchWithStaticProgramShouldThrowEngineExceptionInSpinMethod() {
        Engine brokenEngine = new Engine() {
            @Override
            public void runWashing(int timeInMinutes) throws EngineException {
            }
            @Override
            public void spin() throws EngineException {
                throw new EngineException();
            }
        };
        washingMashine=new WashingMachine(dirtDetector, brokenEngine, waterPump);
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(engineFailue(), result);
    }

    @Test
    void properBatchWithStaticAutoDetectedProgramShouldThrowUnknownErrorException() {
        DirtDetector brokenDirtDetector= laundryBatch -> {
            throw new DirtDetectorException();
        };
        washingMashine=new WashingMachine(brokenDirtDetector, engine, waterPump);
        LaundryStatus result = washingMashine.start(properLaundry, autoDetectedprogramConfiguration);
        assertEquals(LaundryStatus.builder().withErrorCode(ErrorCode.UNKNOWN_ERROR).withRunnedProgram(null).withResult(Result.FAILURE).build(), result);
    }

    @Test
    void properBatchWithStaticAutoDetectedProgramShouldDetectMediumProgram() {
        DirtDetector dirtDetectorWithSmallDirt= laundryBatch -> new Percentage(20d);
        washingMashine=new WashingMachine(dirtDetectorWithSmallDirt, engine, waterPump);
        LaundryStatus result = washingMashine.start(properLaundry, autoDetectedprogramConfiguration);
        assertEquals(success(Program.MEDIUM), result);
    }

    @Test
    void properBatchWithStaticAutoDetectedProgramShouldDetectLongProgram() {
        DirtDetector dirtDetectorWithBigDirt= laundryBatch -> new Percentage(100d);
        washingMashine=new WashingMachine(dirtDetectorWithBigDirt, engine, waterPump);
        LaundryStatus result = washingMashine.start(properLaundry, autoDetectedprogramConfiguration);
        assertEquals(success(staticProgram), result);
    }

    @Test
    void properBatchWithStaticProgramShouldCallEngineandWaterPump() throws WaterPumpException, EngineException {
        washingMashine.start(properLaundry,programConfiguration);

        InOrder callOrder = Mockito.inOrder(waterPump,engine);
        callOrder.verify(waterPump)
                .pour(properWeightKg);
        callOrder.verify(engine)
                .runWashing(staticProgram.getTimeInMinutes());
        callOrder.verify(waterPump)
                .release();
        callOrder.verify(engine)
                .spin();
    }

    @Test
    void properBatchWithAutoDetectedProgramShouldCallEngineandWaterPump() throws WaterPumpException, EngineException {
        DirtDetector dirtDetectorWithBigDirt= laundryBatch -> new Percentage(100d);
        washingMashine=new WashingMachine(dirtDetectorWithBigDirt, engine, waterPump);
        washingMashine.start(properLaundry, autoDetectedprogramConfiguration);

        InOrder callOrder = Mockito.inOrder(waterPump,engine);
        callOrder.verify(waterPump)
                .pour(properWeightKg);
        callOrder.verify(engine)
                .runWashing(staticProgram.getTimeInMinutes());
        callOrder.verify(waterPump)
                .release();
        callOrder.verify(engine)
                .spin();
    }

    private ProgramConfiguration autoDetectProgramWithSpin(Program autoDetectProgram) {
        return ProgramConfiguration.builder().withProgram(autoDetectProgram).withSpin(true).build();
    }

    private LaundryStatus engineFailue() {
        return LaundryStatus.builder().withErrorCode(ErrorCode.ENGINE_FAILURE).withRunnedProgram(staticProgram).withResult(Result.FAILURE).build();
    }

    private LaundryStatus waterPumpFailure() {
        return LaundryStatus.builder()
                .withErrorCode(ErrorCode.WATER_PUMP_FAILURE)
                .withRunnedProgram(staticProgram)
                .withResult(Result.FAILURE).build();
    }

    private LaundryStatus overWeight() {
        return LaundryStatus.builder()
                .withErrorCode(ErrorCode.TOO_HEAVY)
                .withResult(Result.FAILURE)
                .withRunnedProgram(null)
                .build();
    }


    private LaundryStatus success(Program staticProgram) {
        return LaundryStatus.builder()
                .withErrorCode(ErrorCode.NO_ERROR)
                .withResult(Result.SUCCESS)
                .withRunnedProgram(staticProgram)
                .build();
    }

    private ProgramConfiguration staticProgramWithSpin(Program staticProgram) {
        return ProgramConfiguration.builder()
                .withProgram(staticProgram)
                .withSpin(true)
                .build();
    }

    private LaundryBatch batch(Material unrelevant, double properWeightKg) {
        return LaundryBatch.builder()
                .withMaterialType(unrelevant)
                .withWeightKg(properWeightKg)
                .build();
    }

}
