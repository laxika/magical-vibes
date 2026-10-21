package com.github.laxika.magicalvibes.model.condition;

/**
 * The opponent whose attack caused the trigger has at least the required number of creatures
 * attacking this condition's controller, optionally including their planeswalkers.
 * <p>{@code countsRemovedAttackers} is {@code true} when declared attackers that have since left
 * combat still count (the wording is "attacked"). Set it to {@code false} for "are attacking"
 * wording, so a creature removed from combat no longer counts (Tomik, Wielder of Law).</p>
 */
public record OpponentAttacksWithAtLeastCreatures(int minimum, boolean includePlaneswalkers,
                                                 boolean countsRemovedAttackers) implements Condition {

    public OpponentAttacksWithAtLeastCreatures(int minimum, boolean includePlaneswalkers) {
        this(minimum, includePlaneswalkers, true);
    }

    public OpponentAttacksWithAtLeastCreatures(int minimum) {
        this(minimum, true, true);
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
