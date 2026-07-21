package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent controls strictly more lands than the controller (e.g. Gift of Estates:
 * "If an opponent controls more lands than you").
 * <p>
 * When used as an ETB intervening-"if" (CR 603.4), this is a trigger gate — the ability
 * does not go on the stack unless the condition is true as the permanent enters.
 */
public record OpponentControlsMoreLands() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent controls more lands";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent controls more lands than you";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
