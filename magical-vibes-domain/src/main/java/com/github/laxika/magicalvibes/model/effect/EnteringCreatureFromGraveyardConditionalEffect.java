package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

/**
 * Gates an ally-creature-enters trigger on the creature entering from a graveyard or being cast
 * from a graveyard.
 */
public record EnteringCreatureFromGraveyardConditionalEffect(CardEffect wrapped)
        implements EnterCreatureConditionalEffect {

    @Override
    public boolean testEnteringCreature(Card enteringCreature) {
        return false;
    }

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null
                && (enteringPermanent.getEnteredFromGraveyardOwnerId() != null
                || (enteringPermanent.isCast() && enteringPermanent.getCastFromZone() == Zone.GRAVEYARD));
    }

    @Override
    public String triggerDescription(Card enteringCreature) {
        return "it entered or was cast from a graveyard";
    }
}
