package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillHalfDefendingPlayerEffect;

@CardRegistration(set = "BRO", collectorNumber = "83")
public class TerisianMindbreaker extends Card {

    public TerisianMindbreaker() {
        addEffect(EffectSlot.ON_ATTACK, new MillHalfDefendingPlayerEffect());
        addUnearth("{1}{U}{U}{U}");
    }
}
