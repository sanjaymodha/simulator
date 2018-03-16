package com.riskcare.simulator;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by smodha on 12/03/2018.
 */
public class HistoricalPrices {
    private LocalDate date;
    private BigDecimal closingPrice;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getClosingPrice() {
        return closingPrice;
    }

    public void setClosingPrice(BigDecimal closingPrice) {
        this.closingPrice = closingPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoricalPrices that = (HistoricalPrices) o;

        if (!date.equals(that.date)) return false;
        return closingPrice.equals(that.closingPrice);

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + closingPrice.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "HistoricalPrices{" +
                "date=" + date +
                ", closingPrice=" + closingPrice +
                '}';
    }
}
