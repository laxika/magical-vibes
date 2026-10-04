package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsThenControllerDrawsEffect;

@CardRegistration(set = "ONS", collectorNumber = "175")
@CardRegistration(set = "HOP", collectorNumber = "42")
@CardRegistration(set = "MSC", collectorNumber = "159")
@CardRegistration(set = "CMD", collectorNumber = "104")
@CardRegistration(set = "C14", collectorNumber = "165")
@CardRegistration(set = "M3C", collectorNumber = "206")
@CardRegistration(set = "FIC", collectorNumber = "288")
@CardRegistration(set = "C17", collectorNumber = "127")
@CardRegistration(set = "SCD", collectorNumber = "111")
@CardRegistration(set = "CMA", collectorNumber = "70")
public class SyphonMind extends Card {

    public SyphonMind() {
        addEffect(EffectSlot.SPELL, new EachOpponentDiscardsThenControllerDrawsEffect());
    }
}
