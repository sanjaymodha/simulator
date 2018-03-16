package com.riskcare.simulator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by smodha on 12/03/2018.
 */
public class BonusCapCertificate {
    private int barrier;
    private int cap;
    private BigDecimal bonusAmount;
    private BigDecimal multiplier;
    private LocalDate issueDate;
    private LocalDate finalValuationDate;
    private LocalDate maturityDate;
    private BigDecimal priipPrice;
    private BigDecimal stockPrice;
    private LocalDate simulationStartDate;
    private BigDecimal rate;
    private int numberOfSimulations;
    private String qLabDemo;
    private String mrmResultsSheetName;
    private BigDecimal brownianToleranceUnit;
    private String rScriptPath;
    private String summaryOutput;
    private BigDecimal riskFreeRate;

    public int getBarrier() {
        return barrier;
    }

    public void setBarrier(int barrier) {
        this.barrier = barrier;
    }

    public int getCap() {
        return cap;
    }

    public void setCap(int cap) {
        this.cap = cap;
    }

    public BigDecimal getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getFinalValuationDate() {
        return finalValuationDate;
    }

    public void setFinalValuationDate(LocalDate finalValuationDate) {
        this.finalValuationDate = finalValuationDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public BigDecimal getPriipPrice() {
        return priipPrice;
    }

    public void setPriipPrice(BigDecimal priipPrice) {
        this.priipPrice = priipPrice;
    }

    public BigDecimal getStockPrice() {
        return stockPrice;
    }

    public void setStockPrice(BigDecimal stockPrice) {
        this.stockPrice = stockPrice;
    }

    public LocalDate getSimulationStartDate() {
        return simulationStartDate;
    }

    public void setSimulationStartDate(LocalDate simulationStartDate) {
        this.simulationStartDate = simulationStartDate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public int getNumberOfSimulations() {
        return numberOfSimulations;
    }

    public void setNumberOfSimulations(int numberOfSimulations) {
        this.numberOfSimulations = numberOfSimulations;
    }

    public String getqLabDemo() {
        return qLabDemo;
    }

    public void setqLabDemo(String qLabDemo) {
        this.qLabDemo = qLabDemo;
    }

    public String getMrmResultsSheetName() {
        return mrmResultsSheetName;
    }

    public void setMrmResultsSheetName(String mrmResultsSheetName) {
        this.mrmResultsSheetName = mrmResultsSheetName;
    }

    public BigDecimal getBrownianToleranceUnit() {
        return brownianToleranceUnit;
    }

    public void setBrownianToleranceUnit(BigDecimal brownianToleranceUnit) {
        this.brownianToleranceUnit = brownianToleranceUnit;
    }

    public String getrScriptPath() {
        return rScriptPath;
    }

    public void setrScriptPath(String rScriptPath) {
        this.rScriptPath = rScriptPath;
    }

    public String getSummaryOutput() {
        return summaryOutput;
    }

    public void setSummaryOutput(String summaryOutput) {
        this.summaryOutput = summaryOutput;
    }

    public BigDecimal getRiskFreeRate() {
        return riskFreeRate;
    }

    public void setRiskFreeRate(BigDecimal riskFreeRate) {
        this.riskFreeRate = riskFreeRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BonusCapCertificate that = (BonusCapCertificate) o;

        if (barrier != that.barrier) return false;
        if (cap != that.cap) return false;
        if (numberOfSimulations != that.numberOfSimulations) return false;
        if (!bonusAmount.equals(that.bonusAmount)) return false;
        if (!multiplier.equals(that.multiplier)) return false;
        if (!issueDate.equals(that.issueDate)) return false;
        if (!finalValuationDate.equals(that.finalValuationDate)) return false;
        if (!maturityDate.equals(that.maturityDate)) return false;
        if (!priipPrice.equals(that.priipPrice)) return false;
        if (!stockPrice.equals(that.stockPrice)) return false;
        if (!simulationStartDate.equals(that.simulationStartDate)) return false;
        if (!rate.equals(that.rate)) return false;
        if (!qLabDemo.equals(that.qLabDemo)) return false;
        if (!mrmResultsSheetName.equals(that.mrmResultsSheetName)) return false;
        if (!brownianToleranceUnit.equals(that.brownianToleranceUnit)) return false;
        if (!rScriptPath.equals(that.rScriptPath)) return false;
        if (!summaryOutput.equals(that.summaryOutput)) return false;
        return riskFreeRate.equals(that.riskFreeRate);

    }

    @Override
    public int hashCode() {
        int result = barrier;
        result = 31 * result + cap;
        result = 31 * result + bonusAmount.hashCode();
        result = 31 * result + multiplier.hashCode();
        result = 31 * result + issueDate.hashCode();
        result = 31 * result + finalValuationDate.hashCode();
        result = 31 * result + maturityDate.hashCode();
        result = 31 * result + priipPrice.hashCode();
        result = 31 * result + stockPrice.hashCode();
        result = 31 * result + simulationStartDate.hashCode();
        result = 31 * result + rate.hashCode();
        result = 31 * result + numberOfSimulations;
        result = 31 * result + qLabDemo.hashCode();
        result = 31 * result + mrmResultsSheetName.hashCode();
        result = 31 * result + brownianToleranceUnit.hashCode();
        result = 31 * result + rScriptPath.hashCode();
        result = 31 * result + summaryOutput.hashCode();
        result = 31 * result + riskFreeRate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BonusCapCertificate{" +
                "barrier=" + barrier +
                ", cap=" + cap +
                ", bonusAmount=" + bonusAmount +
                ", multiplier=" + multiplier +
                ", issueDate=" + issueDate +
                ", finalValuationDate=" + finalValuationDate +
                ", maturityDate=" + maturityDate +
                ", priipPrice=" + priipPrice +
                ", stockPrice=" + stockPrice +
                ", simulationStartDate=" + simulationStartDate +
                ", rate=" + rate +
                ", numberOfSimulations=" + numberOfSimulations +
                ", qLabDemo='" + qLabDemo + '\'' +
                ", mrmResultsSheetName='" + mrmResultsSheetName + '\'' +
                ", brownianToleranceUnit=" + brownianToleranceUnit +
                ", rScriptPath='" + rScriptPath + '\'' +
                ", summaryOutput='" + summaryOutput + '\'' +
                ", riskFreeRate=" + riskFreeRate +
                '}';
    }
}
