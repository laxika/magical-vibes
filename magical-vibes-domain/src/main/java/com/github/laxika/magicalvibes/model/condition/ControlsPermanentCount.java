package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The controller controls at least {@code minCount} permanents matching the predicate. */
public record ControlsPermanentCount(int minCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " matching permanents";
    }

    /**
     * As an ETB intervening-"if" (CR 603.4) — "When ~ enters, if you control N or more [type],
     * ..." (e.g. Gwyllion Hedge-Mage) — this is a game-state gate checked as the trigger goes on
     * the stack, not a cast-time choice. A gated targeted ETB defers its target to trigger time.
     */
    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
