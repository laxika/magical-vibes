package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

/**
 * Gates an ally-creature-enters trigger on a nontoken creature not cast from hand.
 * When {@code requiresCast} is true, the entering creature must have been cast.
 */
public record EnteringCreatureNotCastFromHandConditionalEffect(CardEffect wrapped, boolean requiresCast)
        implements EnterCreatureConditionalEffect {

    public EnteringCreatureNotCastFromHandConditionalEffect(CardEffect wrapped) {
        this(wrapped, false);
    }

    @Override
    public boolean testEnteringCreature(Card enteringCreature) {
        return false;
    }

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null
                && !enteringPermanent.getCard().isToken()
                && (!requiresCast || enteringPermanent.isCast())
                && (!enteringPermanent.isCast() || enteringPermanent.getCastFromZone() != Zone.HAND);
    }

    @Override
    public String triggerDescription(Card enteringCreature) {
        return "it was not cast from its controller's hand";
    }
}
