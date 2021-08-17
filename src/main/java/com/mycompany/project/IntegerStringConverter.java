package com.mycompany.project;

import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.util.StringConverter;

public class IntegerStringConverter extends StringConverter<Integer> {

    private Runnable _reset;

    private IntegerStringConverter(TextField input, int min, int max) {
        if (input == null)
            throw new NullPointerException("input");

        final int resetValue = Math.min(Math.max(0, min), max);
        _reset = () -> input.setText(Integer.toString(resetValue));

        // bound JavaFX properties cannot be explicitly set
        if (!input.tooltipProperty().isBound())
            input.setTooltip(new Tooltip(String.format(
                    "Enter a value between %d and %d", min, max)));

        // restrict direct input to valid numerical characters
        input.textProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty())
                return;

            // special case: minus sign if negative values allowed
            if (min < 0 && newValue.endsWith("-")) {
                if (newValue.length() > 1)
                    Platform.runLater(() -> input.setText("-"));
                return;
            }

            // revert to oldValue if newValue cannot be parsed
            try {
                Integer.parseInt(newValue);
            }
            catch (NumberFormatException e) {
                Platform.runLater(() -> input.setText(oldValue));
            }
        });

        // validate committed input and restrict to legal range
        final EventHandler<ActionEvent> oldHandler = input.getOnAction();
        input.setOnAction(t -> {
            // fromString performs input validation
            final int value = fromString(input.getText());

            // redundant for Spinner but not harmful
            final int restricted  = Math.min(Math.max(value, min), max);
            if (value != restricted)
                input.setText(Integer.toString(restricted));

            // required for Spinner which handles onAction
            if (oldHandler != null) oldHandler.handle(t);
        });
    }

    static void createFor(Spinner<Integer> spinner) {
        final SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();

        final IntegerStringConverter converter = new IntegerStringConverter(
                spinner.getEditor(), factory.getMin(), factory.getMax());

        factory.setConverter(converter);
        spinner.setTooltip(new Tooltip(String.format(
                "Enter a value between %d and %d",
                factory.getMin(), factory.getMax())));

    }

    @Override
    public Integer fromString(String s) {
        if (s == null || s.isEmpty()) {
            if (_reset != null) _reset.run();
            return 0;
        }

        try {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException e) {
            if (_reset != null) _reset.run();
            return 0;
        }
    }

    @Override
    public String toString(Integer value) {
        if (value == null) return "0";
        return Integer.toString(value);
    }
}
