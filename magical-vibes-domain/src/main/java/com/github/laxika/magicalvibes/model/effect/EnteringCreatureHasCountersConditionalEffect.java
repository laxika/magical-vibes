package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.Locale;

/**
 * Gates an ally-creature-enters trigger on the entering permanent having at least one counter of
 * the specified type.
 */
public record EnteringCreatureHasCountersConditionalEffect(
        CounterType counterType,
        CardEffect wrapped
) implements EnterCreatureConditionalEffect {

    @Override
    public boolean testEnteringCreature(Card enteringCreature) {
        return false;
    }

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null && enteringPermanent.getCounterCount(counterType) > 0;
    }

    @Override
    public String triggerDescription(Card enteringCreature) {
        return "has one or more " + counterType.name().toLowerCase(Locale.ROOT).replace('_', ' ') + " counters";
    }
}
