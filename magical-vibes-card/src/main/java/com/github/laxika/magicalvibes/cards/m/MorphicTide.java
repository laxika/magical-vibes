package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MorphicTideEffect;

@CardRegistration(set = "OPC2", collectorNumber = "3")
public class MorphicTide extends Card {

    public MorphicTide() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new MorphicTideEffect());
    }
}
