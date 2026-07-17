package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a number between {@code minNumber} and {@code maxNumber}." — the resolvable, on-the-stack
 * variant of {@link ChooseNumberOnEnterEffect} (e.g. Shapeshifter's "At the beginning of your upkeep,
 * you may choose a number …"). When it resolves, its handler prompts the source permanent's controller
 * and stores the chosen value via {@code Permanent.getChosenNumber()}. Wrap in a
 * {@link MayEffect} for the optional "you may choose" wording.
 */
public record ChooseNumberEffect(int minNumber, int maxNumber) implements NumberChoiceEffect {
}
