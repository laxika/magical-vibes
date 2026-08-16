package com.github.laxika.magicalvibes.model.condition;

/**
 * The opponent whose attack caused the trigger has at least the required number of creatures
 * attacking this condition's controller or a planeswalker they control.
 */
public record OpponentAttacksWithAtLeastCreatures(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "opponent attacks with at least " + minimum + " creatures at you or your planeswalkers";
    }

    @Override
    public String conditionNotMetReason() {
        return "opponent attacked with fewer than " + minimum + " creatures at you or your planeswalkers";
    }
}
