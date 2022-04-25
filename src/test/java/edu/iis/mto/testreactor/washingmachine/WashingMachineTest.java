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
    Material []firstTypeMaterials;
    Material []secondTypeMaterials;
    double properWeightKg;
    double overWeightKgForFirstTypeMaterials;
    double overWeightKgForSecondTypeMaterials;
    LaundryBatch properLaundry;
    LaundryBatch overWeightLaundryForFirstTypeMaterials;
    LaundryBatch overWeightLaundryForSecondTypeMaterials;
    Program staticProgram;
    Program autoDetectProgram;
    ProgramConfiguration programConfiguration;
    ProgramConfiguration autoDetectedprogramConfiguration;
    Random random;
    @BeforeEach
    void setUp() throws Exception {
        random=new Random();
        unrelevant =Material.COTTON;
        firstTypeMaterials= new Material[]{Material.JEANS, Material.WOOL};
        secondTypeMaterials= new Material[]{Material.SYNTETIC, Material.DELICATE, Material.COTTON};
        properWeightKg=7d;
        overWeightKgForFirstTypeMaterials=5d;
        overWeightKgForSecondTypeMaterials=12d;
        staticProgram = Program.LONG;
        autoDetectProgram=Program.AUTODETECT;
        properLaundry= batch(unrelevant, properWeightKg);
        overWeightLaundryForFirstTypeMaterials=batch(firstTypeMaterials[random.nextInt(firstTypeMaterials.length)], overWeightKgForFirstTypeMaterials);
        overWeightLaundryForSecondTypeMaterials=batch(secondTypeMaterials[random.nextInt(secondTypeMaterials.length)], overWeightKgForSecondTypeMaterials);
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
        LaundryStatus result = washingMashine.start(overWeightLaundryForFirstTypeMaterials,null);
        assertEquals(overWeight(), result);
    }

    @Test
    void overWeightBatchWithoutJeansOrWoolMaterialJeWithNullProgram() {
        LaundryStatus result = washingMashine.start(overWeightLaundryForSecondTypeMaterials,null);
        assertEquals(overWeight(), result);
    }

    @Test
    void properBatchWithStaticProgramShouldThrowWaterPumpExceptionInPourMethod() throws WaterPumpException {
        Mockito.doThrow(new WaterPumpException()).when(waterPump).pour(properWeightKg);
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(waterPumpFailure(), result);
    }

    @Test
    void properBatchWithStaticProgramShouldThrowWaterPumpExceptionInReleaseMethod() throws WaterPumpException {
        Mockito.doThrow(new WaterPumpException()).when(waterPump).release();
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(waterPumpFailure(), result);
    }

    @Test
    void properBatchWithStaticProgramShouldThrowEngineExceptionInRunWashingMethod() throws EngineException {
        Mockito.doThrow(new EngineException()).when(engine).runWashing(staticProgram.getTimeInMinutes());
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(engineFailue(), result);
    }

    @Test
    void properBatchWithStaticProgramShouldThrowEngineExceptionInSpinMethod() throws EngineException {
        Mockito.doThrow(new EngineException()).when(engine).spin();
        LaundryStatus result = washingMashine.start(properLaundry, programConfiguration);
        assertEquals(engineFailue(), result);
    }

    @Test
    void properBatchWithStaticAutoDetectedProgramShouldThrowUnknownErrorException() {
        DirtDetector brokenDirtDetector= laundryBatch -> null;
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
