package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

/**
 * Gates a land-entry trigger on the land entering from somewhere other than its controller's hand.
 */
public record EnteringLandNotFromHandConditionalEffect(CardEffect wrapped)
        implements EnteringLandConditionalEffect {

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null && enteringPermanent.getEnteredFromZone() != Zone.HAND;
    }

    @Override
    public String triggerDescription(Card enteringLand) {
        return "it entered from somewhere other than its controller's hand";
    }
}
