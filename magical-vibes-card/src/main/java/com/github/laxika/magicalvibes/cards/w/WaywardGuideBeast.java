package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ZNR", collectorNumber = "176")
public class WaywardGuideBeast extends Card {

    public WaywardGuideBeast() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                ReturnPermanentControlledByPlayerToHandEffect.controller(
                        new PermanentIsLandPredicate(), "land"));
    }
}
