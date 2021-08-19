package com.mycompany.project;

class CalculationThread extends Thread {
    private static boolean suspendFlag;
    private static boolean stopFlag;
    private long iterationsCount;
    private int currentThreadNumber;
    private int threadsCount;
    private long currentIteration;
    private long result;

    CalculationThread(){
        super();
        result = 0;
        currentIteration = 0;
        suspendFlag = false;
        stopFlag = false;
    }

    static void setSuspendFlag(boolean value){
        suspendFlag = value;
    }

    static void terminate(){
        stopFlag = true;
        suspendFlag = false;
    }

    long getCurrentIteration() {
        return currentIteration;
    }

    long getResult() {
        return result;
    }

    void start(int threadNumber, long length, int allThreadNumber){
        iterationsCount = length;
        currentThreadNumber = threadNumber;
        threadsCount = allThreadNumber;
        super.start();
    }

    @Override
    public void run(){
        try {
            for(currentIteration = this.currentThreadNumber; currentIteration < this.iterationsCount; currentIteration +=this.threadsCount){
                while(suspendFlag) {
                    Thread.sleep(100);
                }
                if (stopFlag) {
                    result = 0;
                    break;
                }
                result +=(currentIteration %2 == 1 ? -1 : 1)*(1+ currentIteration *3);
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
