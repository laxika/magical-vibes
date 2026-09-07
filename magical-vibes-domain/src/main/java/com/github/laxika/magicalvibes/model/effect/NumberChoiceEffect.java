package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability marker for effects that ask the controller to choose a number in an inclusive range
 * ("choose a number between {@code minNumber()} and {@code maxNumber()}"). The chosen value is
 * stored on the source permanent via {@code Permanent.getChosenNumber()} and read by
 * {@link com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource}. Implemented by both the
 * "as this enters" variants ({@link ChooseNumberOnEnterEffect},
 * {@link ChooseRandomNumberOnEnterEffect}) and the resolvable-on-the-stack variant
 * ({@link ChooseNumberEffect}).
 */
public interface NumberChoiceEffect extends CardEffect {

    int minNumber();

    int maxNumber();

    /** Whether the number is selected by the engine instead of through player input. */
    default boolean chooseRandomly() {
        return false;
    }
}
