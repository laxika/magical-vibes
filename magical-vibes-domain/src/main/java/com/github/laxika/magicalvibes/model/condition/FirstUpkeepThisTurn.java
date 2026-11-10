package com.github.laxika.magicalvibes.model.condition;

/**
 * The upkeep step that is beginning is the turn's first, not an additional one: "at the beginning of enchanted
 * player's first upkeep each turn" (Paradox Haze), the trigger's event condition.
 */
public record FirstUpkeepThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "the first upkeep this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "an additional upkeep step";
    }
}
