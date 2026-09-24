package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

/**
 * Gates an ally-creature-enters trigger on the creature being nontoken and not having been cast
 * from hand.
 */
public record EnteringCreatureNotCastFromHandConditionalEffect(CardEffect wrapped)
        implements EnterCreatureConditionalEffect {

    @Override
    public boolean testEnteringCreature(Card enteringCreature) {
        return false;
    }

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null
                && !enteringPermanent.getCard().isToken()
                && (!enteringPermanent.isCast() || enteringPermanent.getCastFromZone() != Zone.HAND);
    }

    @Override
    public String triggerDescription(Card enteringCreature) {
        return "it was not cast from its controller's hand";
    }
}
