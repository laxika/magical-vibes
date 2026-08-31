package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

/**
 * Gates a creature-enters trigger on a nontoken creature entering from exile or being cast from
 * exile.
 */
public record EnteringCreatureFromExileConditionalEffect(CardEffect wrapped)
        implements EnterCreatureConditionalEffect {

    @Override
    public boolean testEnteringCreature(Card enteringCreature) {
        return false;
    }

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null
                && !enteringPermanent.getCard().isToken()
                && (enteringPermanent.getEnteredFromZone() == Zone.EXILE
                || (enteringPermanent.isCast() && enteringPermanent.getCastFromZone() == Zone.EXILE));
    }

    @Override
    public String triggerDescription(Card enteringCreature) {
        return "it entered or was cast from exile";
    }
}
