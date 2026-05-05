package com.billing.dto;

public class DashboardStats {
    private long totalBills;
    private double totalAmount;
    private long pendingCount;
    private long paidCount;
    private long cancelledCount;
    private double pendingAmount;
    private double paidAmount;

    public DashboardStats() {}

    public DashboardStats(long totalBills, double totalAmount, long pendingCount, long paidCount, long cancelledCount, double pendingAmount, double paidAmount) {
        this.totalBills = totalBills;
        this.totalAmount = totalAmount;
        this.pendingCount = pendingCount;
        this.paidCount = paidCount;
        this.cancelledCount = cancelledCount;
        this.pendingAmount = pendingAmount;
        this.paidAmount = paidAmount;
    }

    public long getTotalBills() { return totalBills; }
    public void setTotalBills(long totalBills) { this.totalBills = totalBills; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
    public long getPaidCount() { return paidCount; }
    public void setPaidCount(long paidCount) { this.paidCount = paidCount; }
    public long getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }
    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double pendingAmount) { this.pendingAmount = pendingAmount; }
    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
}
