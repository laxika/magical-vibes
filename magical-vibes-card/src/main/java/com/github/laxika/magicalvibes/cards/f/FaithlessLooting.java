package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;

@CardRegistration(set = "DKA", collectorNumber = "87")
public class FaithlessLooting extends Card {

    public FaithlessLooting() {
        addEffect(EffectSlot.SPELL, new DrawAndDiscardCardEffect(2, 2));
        addCastingOption(new FlashbackCast("{2}{R}"));
    }
}
