package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesWithPowerLessThanTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "40")
public class MandateOfAbaddon extends Card {

    public MandateOfAbaddon() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new DestroyAllCreaturesWithPowerLessThanTargetEffect());
    }
}
