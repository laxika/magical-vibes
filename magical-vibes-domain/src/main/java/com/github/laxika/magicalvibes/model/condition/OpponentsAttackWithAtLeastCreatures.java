package com.github.laxika.magicalvibes.model.condition;

/**
 * The player whose attack caused the trigger is an opponent of this condition's controller and
 * attacks with at least {@code minimum} creatures, whichever player or planeswalker they attack
 * ("whenever two or more creatures your opponents control attack"). Contrast
 * {@link OpponentAttacksWithAtLeastCreatures}, which counts only attackers aimed at the controller.
 */
public record OpponentsAttackWithAtLeastCreatures(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "opponents attack with at least " + minimum + " creatures";
    }

    @Override
    public String conditionNotMetReason() {
        return "opponents attacked with fewer than " + minimum + " creatures";
    }
}
