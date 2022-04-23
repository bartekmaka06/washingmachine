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
    ProgramConfiguration programConfiguration;
    @BeforeEach
    void setUp() throws Exception {
        unrelevant =Material.COTTON;
        properWeightKg=7d;
        staticProgram = Program.LONG;
        properLaundry= batch(unrelevant, properWeightKg);
        programConfiguration= staticProgramWithSpin(staticProgram);
        washingMashine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void properBatchWithStaticProgram() {
        LaundryStatus result = washingMashine.start(properLaundry,programConfiguration);
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
