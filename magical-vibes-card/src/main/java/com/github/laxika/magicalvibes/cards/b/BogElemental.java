package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "PCY", collectorNumber = "57")
public class BogElemental extends Card {

    public BogElemental() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsLandPredicate(), "a land"));
    }
}
