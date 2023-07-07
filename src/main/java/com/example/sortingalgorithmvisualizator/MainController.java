package com.example.sortingalgorithmvisualizator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.util.*;

public class MainController {

    @FXML
    private BorderPane pane;
    @FXML
    private Slider arraySizeSlider, arrayRangeSlider;
    @FXML
    private ComboBox<String> sortingAlgorithmChoice;
    @FXML
    private Label arraySizeValueLabel, arrayRangeValueLabel;
    @FXML
    private Button sortButton, resetButton;
    @FXML
    private Spinner<Integer> delayPicker;

    private static final int DEFAULT_ARRAY_SIZE = 50;
    private static final int DEFAULT_ARRAY_RANGE = 100;
    private static final int MIN_DELAY = 0;
    private static final int DEFAULT_DELAY = 200;
    private static final int MAX_DELAY = 1000;

    private BarChart<String, Number> barChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private static int barsNumber, valueRange;
    private static long currentDelay;
    private String sortingAlgorithm;

    @FXML
    public void initialize() {
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();

        arraySizeSlider.setValue(DEFAULT_ARRAY_SIZE);
        arraySizeValueLabel.setText(Integer.toString(DEFAULT_ARRAY_SIZE));

        arrayRangeSlider.setValue(DEFAULT_ARRAY_RANGE);
        arrayRangeValueLabel.setText(Integer.toString(DEFAULT_ARRAY_RANGE));

        SpinnerValueFactory<Integer> delaySpinner = new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_DELAY, MAX_DELAY);
        delaySpinner.setValue(DEFAULT_DELAY);
        delayPicker.setValueFactory(delaySpinner);

        barsNumber = DEFAULT_ARRAY_SIZE;
        valueRange = DEFAULT_ARRAY_RANGE;
        currentDelay = DEFAULT_DELAY;

        fillArray();

        arraySizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            barsNumber = (int) arraySizeSlider.getValue();
            arraySizeValueLabel.setText(Integer.toString(barsNumber));
        });

        arrayRangeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            valueRange = (int) arrayRangeSlider.getValue();
            arrayRangeValueLabel.setText(Integer.toString(valueRange));
        });

        delayPicker.valueProperty().addListener((observable, oldValue, newValue) -> currentDelay = delayPicker.getValue());

        sortingAlgorithmChoice.setItems(FXCollections.observableArrayList("Selection sort", "Bubble sort", "Insertion sort", "Quick sort", "Merge sort"));
    }

    @FXML
    public void handleAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About us");
        alert.setHeaderText("Hi, we're two Computer Engineers students at UNIMORE, University of Modena and Reggio Emilia");
        alert.setContentText("Here some references:\nGabriele Aldovardi -> GitHub: https://github" +
                ".com/GabrieleAldovardi\nFilippo Cavalieri -> GitHub: https://github.com/FilippoCavalieri");
        alert.showAndWait();
    }

    public void initializeBarChart() {
        barChart.setLegendVisible(false);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setHorizontalZeroLineVisible(false);
        barChart.setVerticalZeroLineVisible(false);
        barChart.setBarGap(0);
        barChart.setAnimated(false);
    }

    public void fillArray() {
        XYChart.Series series = new XYChart.Series();
        barChart = new BarChart(xAxis, yAxis);
        initializeBarChart();
        for (int i = 0; i < barsNumber; i++) {
            series.getData().add(new XYChart.Data(Integer.toString(i), new Random().nextInt(valueRange) + 1));
        }
        barChart.getData().add(series);
        pane.setCenter(barChart);

        for (int i = 0; i < barsNumber; i++)
            ((XYChart.Data<String, Integer>) series.getData().get(i)).getNode().setStyle("-fx-background-color:#00D8FA");
    }

    public static void delay() {
        try {
            Thread.sleep(currentDelay);
        } catch (InterruptedException ignored) {

        }
    }

    @FXML
    public void handleReset() throws InterruptedException {
        barChart.getData().clear();
        fillArray();
        sortButton.setDisable(false);
    }

    @FXML
    public void handleSort() {
        //resetButton.setDisable(true);
        sortButton.setDisable(true);
        try {
            sortingAlgorithm = sortingAlgorithmChoice.getSelectionModel().getSelectedItem().toString();
        } catch (Exception e) {
            showNoSelectedAlgorithmAlert();
        }
        Task task = new Task() {
            @Override
            protected Object call() {
                try {
                    switch (sortingAlgorithm) {
                        case "Selection sort" -> SortingAlgorithms.selectionSort(barChart.getData().get(0).getData());
                        case "Bubble sort" -> SortingAlgorithms.bubbleSort(barChart.getData().get(0).getData());
                        case "Insertion sort" -> SortingAlgorithms.insertionSort(barChart.getData().get(0).getData());
                        case "Quick sort" -> SortingAlgorithms.quickSort(barChart.getData().get(0).getData());
                        case "Merge sort" -> SortingAlgorithms.mergeSort(barChart.getData().get(0).getData());
                        default -> throw new Exception();
                    }
                } catch (NullPointerException ignored) {

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    resetButton.setDisable(false);
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    public void showNoSelectedAlgorithmAlert() {
        sortButton.setDisable(false);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Selection");
        alert.setHeaderText("No algorithm selected");
        alert.setContentText("Please select an algorithm in the check box.");
        alert.showAndWait();
    }

    public static class SortingAlgorithms {
        private static void swap(ObservableList<XYChart.Data<String, Number>> list, int index1, int index2) {
            Number tmp = list.get(index1).getYValue();
            list.get(index1).setYValue(list.get(index2).getYValue());
            list.get(index2).setYValue(tmp);
        }

        private static int findMax(ObservableList<XYChart.Data<String, Number>> list, int range) {
            int maxIndex = 0; //Hp: first element is the max
            for (int i = 1; i < range; ++i) {
                if (list.get(i).getYValue().intValue() > list.get(maxIndex).getYValue().intValue()) {
                    delay();
                    maxIndex = i;
                }
            }
            return maxIndex;
        }

        public static void selectionSort(ObservableList<XYChart.Data<String, Number>> list) {
            int maxIndex;
            for (int listSize = list.size(); listSize > 1; listSize--) {
                maxIndex = findMax(list, listSize);
                if (maxIndex < listSize - 1) {
                    delay();
                    swap(list, maxIndex, listSize - 1);
                }
            }
        }

        public static void bubbleSort(ObservableList<XYChart.Data<String, Number>> list) {
            boolean ordered = false;
            for (int listSize = list.size(); listSize > 1 && !ordered; listSize--) {
                ordered = true; //Hp: the list is ordered
                for (int i = 0; i < listSize - 1; i++) {
                    if (list.get(i).getYValue().intValue() >= list.get(i + 1).getYValue().intValue()) {
                        delay();
                        swap(list, i, i + 1);
                        ordered = false;
                    }
                }
            }
        }

        private static void insertMin(ObservableList<XYChart.Data<String, Number>> list, int lastPos) {
            int i, lastValue = list.get(lastPos).getYValue().intValue();
            for (i = lastPos - 1; i >= 0 && lastValue < list.get(i).getYValue().intValue(); i--) {
                delay();
                swap(list, i + 1, i);
            }
            list.get(i + 1).setYValue(lastValue);
        }

        public static void insertionSort(ObservableList<XYChart.Data<String, Number>> list) {
            for (int i = 1; i < list.size(); i++) {
                insertMin(list, i);
            }
        }

        private static void quickSortRec(ObservableList<XYChart.Data<String, Number>> list, int first, int last) {
            int i, j, pivot;
            if (first < last) {
                i = first;
                j = last;
                pivot = list.get((first + last) / 2).getYValue().intValue();

                do {
                    for (; list.get(i).getYValue().intValue() < pivot; i++)
                        ;
                    for (; list.get(j).getYValue().intValue() > pivot; j--)
                        ;

                    if (i <= j) {
                        delay();
                        swap(list, i, j);
                        i++;
                        j--;
                    }
                } while (i <= j);
                quickSortRec(list, first, j);
                quickSortRec(list, i, last);
            }
        }

        public static void quickSort(ObservableList<XYChart.Data<String, Number>> list) {
            quickSortRec(list, 0, list.size() - 1);
        }

        private static void mergeSort(ObservableList<XYChart.Data<String, Number>> list) {
            mergeSortRec(list, 0, barsNumber - 1);
        }

        private static void mergeSortRec(ObservableList<XYChart.Data<String, Number>> list, int start, int end) {
            int mid;
            if (start < end) {
                mid = (start + end) / 2;

                mergeSortRec(list, start, mid);
                mergeSortRec(list, mid + 1, end);

                mergeOperation(list, start, mid, end);
            }
        }
        private static void mergeOperation(ObservableList<XYChart.Data<String, Number>> list, int start, int mid,
                                           int end) {
            int i = start, j = mid + 1, k = start;

            List<Number> tmp = new ArrayList<>();

            for(XYChart.Data<String, Number> data : list){
                tmp.add(data.getYValue());
            }

            while (i <= mid && j <= end) {
                if ((int) tmp.get(i) < (int) tmp.get(j)) {
                    delay();
                    (list.get(k)).setYValue(tmp.get(i));
                    i++;
                    k++;
                } else {
                    delay();
                    (list.get(k)).setYValue(tmp.get(j));
                    j++;
                    k++;
                }
            }
            for (; i <= mid; i++) {
                delay();
                (list.get(k)).setYValue(tmp.get(i));
                k++;
            }
            for (; j <= end; j++) {
                delay();
                (list.get(k)).setYValue( tmp.get(j));
                k++;
            }

        }
    }
}
