package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;

@CardRegistration(set = "DKA", collectorNumber = "104")
public class ShatteredPerception extends Card {

    public ShatteredPerception() {
        addEffect(EffectSlot.SPELL, new DiscardOwnHandThenDrawThatManyEffect());
        addCastingOption(new FlashbackCast("{5}{R}"));
    }
}
