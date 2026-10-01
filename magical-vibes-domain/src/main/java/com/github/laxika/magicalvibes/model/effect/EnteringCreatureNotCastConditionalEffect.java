package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Gates an ally-creature-enters trigger on the entering creature not having been cast.
 */
public record EnteringCreatureNotCastConditionalEffect(CardEffect wrapped)
        implements EnterCreatureConditionalEffect {

    @Override
    public boolean testEnteringCreature(Card enteringCreature) {
        return false;
    }

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null && !enteringPermanent.isCast();
    }

    @Override
    public String triggerDescription(Card enteringCreature) {
        return "it wasn't cast";
    }
}
