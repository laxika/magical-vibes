package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextTurnEffect;

@CardRegistration(set = "TMP", collectorNumber = "76")
public class Meditate extends Card {

    public Meditate() {
        // Draw four cards. You skip your next turn.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        addEffect(EffectSlot.SPELL, new SkipNextTurnEffect());
    }
}
