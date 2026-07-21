package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Conditional wrapper for ally-creature-enters triggers: the wrapped effect only fires if the
 * entering creature's printed power and toughness both equal the given values.
 * <p>
 * Used by cards like Sigil Captain ("whenever a creature you control enters, if that creature is
 * 1/1, put two +1/+1 counters on it") — the "is 1/1" intervening-if gate.
 */
public record EnteringCreatureExactStatsConditionalEffect(
        int power,
        int toughness,
        CardEffect wrapped
) implements EnterCreatureConditionalEffect {

    @Override
    public boolean testEnteringCreature(Card enteringCreature) {
        return enteringCreature.getPower() != null && enteringCreature.getToughness() != null
                && enteringCreature.getPower() == power && enteringCreature.getToughness() == toughness;
    }

    @Override
    public String triggerDescription(Card enteringCreature) {
        return "is " + power + "/" + toughness;
    }
}
