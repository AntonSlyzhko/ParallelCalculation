package com.mycompany.project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class Controller {

    class BackgroundThread extends Thread {
        private int allThreadNumber;
        private long numberOfIterations;
        boolean isTerminated;
        boolean isExited;
        private CalculationThread[] threadArray;

        BackgroundThread(){
            super();
            isTerminated = false;
            isExited = false;
        }

        class UpdateProgressThread extends Thread{
            private int allThreadNumber;
            private boolean shouldIContinue;
            double progress;

            UpdateProgressThread(int allThreadNumber){
                super();
                shouldIContinue = true;
                this.allThreadNumber = allThreadNumber;
            }

            void stopUpdating(){
                shouldIContinue = false;
            }

            void progressRollBack(){
                for(double i = progress; i >=0; i = i - 0.005){
                    progressBar.setProgress(i);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void run(){
                long iSum;
                while (shouldIContinue){
                    iSum = 0;
                    for(int j = 0; j < allThreadNumber; j++)
                        iSum += threadArray[j].getCurrentIteration();
                    progress = ((double) iSum/numberOfIterations)/allThreadNumber;
                    progressBar.setProgress(progress);
                }
            }

        }

        void terminate(){
            CalculationThread.terminate();
            isTerminated = true;
        }

        private void setAllThreadNumber(){
            allThreadNumber = Integer.parseInt(getNumberOfThreadsField().getText());
        }

        private void setNumberOfIterations(){
            numberOfIterations = getNumberOfIterationsSpinner().getValue();
        }

        private void textToPrint(String newText) {
            synchronized (BackgroundThread.class){
                Runnable updater = () -> processTextOut.appendText("\n"+newText);
                Platform.runLater(updater);
            }
        }

        @Override
        public void run() {
            int i;
            setNumberOfIterations();
            setAllThreadNumber();
            threadArray = new CalculationThread[allThreadNumber];
            for (i = 0; i < allThreadNumber; i++)
                threadArray[i] = new CalculationThread();

            UpdateProgressThread myUpdateProgressThread = new UpdateProgressThread(allThreadNumber);
            myUpdateProgressThread.setPriority(MIN_PRIORITY);
            myUpdateProgressThread.start();

            for (i = 0; i < allThreadNumber; i++)
                threadArray[i].start(i,numberOfIterations, allThreadNumber);

            long finalResult = 0;
            for (i = 0; i < allThreadNumber; i++){
                try{
                    threadArray[i].join();
                    finalResult +=threadArray[i].getResult();
                }catch (InterruptedException exception){
                    exception.printStackTrace();
                }
            }

            if(!isExited){
                String text;
                if (!isTerminated){
                    text = "Calculation has been completed.\nResult: "+ finalResult +".";
                    textToPrint(text);
                    myUpdateProgressThread.stopUpdating();
                }else{
                    text = "Calculation has been terminated.";
                    textToPrint(text);
                    myUpdateProgressThread.stopUpdating();
                    myUpdateProgressThread.progressRollBack();
                }
                unblockButton();
            }
        }

    }


    private BackgroundThread myBackgroundThread;

    private boolean firstTime = true;

    private String oldNumber = "4";

    @FXML
    private Button startCalcButton;

    @FXML
    public TextArea processTextOut;

    @FXML
    private Spinner<Integer> numberOfIterationsSpinner;

    @FXML
    protected Button stopButton;

    @FXML
    private Button resumeButton;

    @FXML
    private Button pauseButton;

    @FXML
    private TextField numberOfThreadsField;

    @FXML
    public ProgressBar progressBar;

    public void pause(){
        CalculationThread.setSuspendFlag(true);
    }

    public void resume(){
        CalculationThread.setSuspendFlag(false);
    }

    public void stop(){
        myBackgroundThread.terminate();
    }

    private Spinner<Integer> getNumberOfIterationsSpinner() {
        return numberOfIterationsSpinner;
    }

    private TextField getNumberOfThreadsField() {
        return numberOfThreadsField;
    }

    private void unblockButton(){
        resumeButton.setDisable(true);
        stopButton.setDisable(true);
        pauseButton.setDisable(true);
        startCalcButton.setDisable(false);
    }

    private boolean checkForInt(String tmp){
        try{
            int tmpInt;
            tmpInt = Integer.parseInt(tmp);
            if (tmpInt >= 1 && tmpInt <= 100)
                oldNumber = tmp;
            return true;
        }catch (NumberFormatException exception){
            return false;
        }
    }

    public void validateThreadsNumber(){
        if (!numberOfThreadsField.getText().isEmpty()){
            String number = numberOfThreadsField.getText();
            if (checkForInt(number)){
                int newThreadNumber = Integer.parseInt(number);
                if (newThreadNumber < 1){
                    numberOfThreadsField.setText("1");
                    oldNumber = "1";
                }
                else if (newThreadNumber > 100){
                    numberOfThreadsField.setText("100");
                    oldNumber = "100";
                }

            }else
                numberOfThreadsField.setText(oldNumber);
        }
        numberOfThreadsField.positionCaret(numberOfThreadsField.getText().length());
    }

    public void startCalc(){
        if (!numberOfThreadsField.getText().isEmpty()){
            startCalcButton.setDisable(true);
            resumeButton.setDisable(false);
            stopButton.setDisable(false);
            pauseButton.setDisable(false);
            if (!firstTime){
                processTextOut.appendText("\n\n");
                firstTime = false;
            }
            processTextOut.appendText("Start parallel calculation. Number of threads: "+numberOfThreadsField.getText());
            processTextOut.appendText(".\nNumber of iterations: "+numberOfIterationsSpinner.getValue()+"\n");
            myBackgroundThread = new BackgroundThread();
            myBackgroundThread.start();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Application will work properly only if you enter all values!!!");
            alert.setContentText(null);
            alert.showAndWait();
        }

    }

    @FXML
    void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 2147483646, 10000);
        numberOfIterationsSpinner.setValueFactory(valueFactory);
        IntegerStringConverter.createFor(numberOfIterationsSpinner);
        numberOfThreadsField.setTooltip(new Tooltip(String.format("Enter a value between %d and %d", 1, 100)));

        try {
            ImageView stopView = new ImageView(new Image(getClass().getResourceAsStream("stop.png")));
            stopView.setFitHeight(50);
            stopView.setPreserveRatio(true);
            stopButton.setGraphic(stopView);
        }catch (NullPointerException e){
            System.out.println("There is no \'stop.png\' image in recources folder.");
            stopButton.setText("Stop");
        }

        try {
            ImageView resumeView = new ImageView(new Image(getClass().getResourceAsStream("resume.png")));
            resumeView.setFitHeight(50);
            resumeView.setPreserveRatio(true);
            resumeButton.setGraphic(resumeView);
        }catch (NullPointerException e){
            System.out.println("There is no \'resume.png\' image in recources folder.");
            resumeButton.setText("Resume");
        }

        try {
            ImageView pauseView = new ImageView(new Image(getClass().getResourceAsStream("pause.png")));
            pauseView.setFitHeight(50);
            pauseView.setPreserveRatio(true);
            pauseButton.setGraphic(pauseView);
        }catch (NullPointerException e){
            System.out.println("There is no \'pause.png\' image in recources folder.");
            pauseButton.setText("Pause");
        }

        progressBar.setStyle("-fx-accent: darkred;");

        resumeButton.setDisable(true);
        stopButton.setDisable(true);
        pauseButton.setDisable(true);

    }
}






