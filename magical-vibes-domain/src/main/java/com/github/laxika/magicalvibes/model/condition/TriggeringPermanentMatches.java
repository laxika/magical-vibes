package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The permanent the trigger event was about (the stack entry's triggering permanent) matches the predicate as the
 * effect resolves: "draw a card if that creature has a +1/+1 counter on it". A permanent that left the battlefield is
 * judged as it last existed there. A filter on what triggers the ability is the trigger's own
 * ({@code Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(...)}), not this.
 */
public record TriggeringPermanentMatches(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent matches " + filter;
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering permanent does not match " + filter;
    }
}
