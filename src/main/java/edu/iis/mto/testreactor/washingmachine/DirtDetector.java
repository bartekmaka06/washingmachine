package edu.iis.mto.testreactor.washingmachine;

public interface DirtDetector {

    Percentage detectDirtDegree(LaundryBatch laundryBatch) throws DirtDetectorException;

}
