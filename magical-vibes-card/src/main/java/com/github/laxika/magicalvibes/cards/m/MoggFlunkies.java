package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "M13", collectorNumber = "143")
@CardRegistration(set = "STH", collectorNumber = "92")
@CardRegistration(set = "TPR", collectorNumber = "145")
@CardRegistration(set = "ATH", collectorNumber = "45")
public class MoggFlunkies extends Card {

    public MoggFlunkies() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect());
    }
}
