package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffect;

@CardRegistration(set = "SOS", collectorNumber = "203")
public class MindRoots extends Card {

    public MindRoots() {
        addEffect(EffectSlot.SPELL, new TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffect(2));
    }
}
