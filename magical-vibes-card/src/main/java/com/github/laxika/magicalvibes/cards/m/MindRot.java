package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "159")
@CardRegistration(set = "M10", collectorNumber = "105")
@CardRegistration(set = "M11", collectorNumber = "105")
public class MindRot extends Card {

    public MindRot() {
        addEffect(EffectSlot.SPELL, new TargetPlayerDiscardsEffect(2));
    }
}
