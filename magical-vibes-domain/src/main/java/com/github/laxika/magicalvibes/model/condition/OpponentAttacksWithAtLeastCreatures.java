package com.github.laxika.magicalvibes.model.condition;

/**
 * The opponent whose attack caused the trigger has at least the required number of creatures
 * attacking this condition's controller, optionally including their planeswalkers.
 */
public record OpponentAttacksWithAtLeastCreatures(int minimum, boolean includePlaneswalkers) implements Condition {

    public OpponentAttacksWithAtLeastCreatures(int minimum) {
        this(minimum, true);
    }

    @Override
    public String conditionName() {
        return includePlaneswalkers
                ? "opponent attacks with at least " + minimum + " creatures at you or your planeswalkers"
                : "opponent attacks you directly with at least " + minimum + " creatures";
    }

    @Override
    public String conditionNotMetReason() {
        return includePlaneswalkers
                ? "opponent attacked with fewer than " + minimum + " creatures at you or your planeswalkers"
                : "opponent attacked you directly with fewer than " + minimum + " creatures";
    }
}
