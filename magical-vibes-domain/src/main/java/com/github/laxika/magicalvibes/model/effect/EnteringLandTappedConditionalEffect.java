package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

/** Gates a landfall trigger on the entering land being tapped. */
public record EnteringLandTappedConditionalEffect(CardEffect wrapped)
        implements EnteringLandConditionalEffect {

    @Override
    public boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null && enteringPermanent.isTapped();
    }

    @Override
    public String triggerDescription(Card enteringLand) {
        return "it entered the battlefield tapped";
    }
}
