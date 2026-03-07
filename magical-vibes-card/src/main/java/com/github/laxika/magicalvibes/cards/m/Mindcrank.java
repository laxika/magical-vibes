package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "144")
public class Mindcrank extends Card {

    public Mindcrank() {
        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE, new MillOpponentOnLifeLossEffect());
    }
}
