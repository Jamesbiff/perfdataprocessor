package roq.eps.perfdataprocessor;

public class PerfModel {
    
    protected String scenario;

    protected Double cpuMax = 0D;
    protected Double cpuMin = 0D;
    protected Double cpuAvg = 0D;
    private Double cpuTot = 0D;
    private Double cpuCount = 0D;
    
    protected Double memMax = 0D;
    protected Double memMin = 0D;
    protected Double memAvg = 0D;
    private Double memTot = 0D;
    private Double memCount = 0D;

    PerfModel(String scenario) {
        this.scenario = scenario;
    }

    void updateCpu(Double value ) {
        if (cpuCount == 0D) {
            cpuMin = value;
        }
        if (value > cpuMax) {
            cpuMax = value;
        }
        if (value < cpuMin) {
            cpuMin = value;
        }
        cpuTot = cpuTot + value;
        cpuCount++;
        cpuAvg = cpuTot / cpuCount;
    }

    void updateMem(Double value ) {
        if (memCount == 0D) {
            memMin = value;
        }
        if (value > memMax) {
            memMax = value;
        }
        if (value < memMin) {
            memMin = value;
        }
        memTot = memTot + value;
        memCount++;
        memAvg = memTot / memCount;
    }

}
