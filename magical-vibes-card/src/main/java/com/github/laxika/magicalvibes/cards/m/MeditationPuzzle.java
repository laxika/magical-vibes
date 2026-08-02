package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M15", collectorNumber = "19")
public class MeditationPuzzle extends Card {

    public MeditationPuzzle() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(8));
    }
}
