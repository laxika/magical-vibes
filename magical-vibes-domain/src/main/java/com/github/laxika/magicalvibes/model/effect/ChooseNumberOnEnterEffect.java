package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this permanent enters, choose a number between {@code minNumber} and {@code maxNumber}."
 * (e.g. Shapeshifter). A static ability (CR 614.1c): the engine prompts the
 * controller for the number as the permanent enters and stores it via
 * {@code Permanent.getChosenNumber()}.
 */
public record ChooseNumberOnEnterEffect(int minNumber, int maxNumber) implements NumberChoiceEffect {
}
