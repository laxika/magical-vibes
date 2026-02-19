package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "159")
public class MindRot extends Card {

    public MindRot() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new TargetPlayerDiscardsEffect(2));
    }
}
