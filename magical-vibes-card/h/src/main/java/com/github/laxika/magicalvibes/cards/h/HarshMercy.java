package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesExceptChosenTypesEffect;

@CardRegistration(set = "ONS", collectorNumber = "39")
public class HarshMercy extends Card {

    public HarshMercy() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesExceptChosenTypesEffect());
    }
}
