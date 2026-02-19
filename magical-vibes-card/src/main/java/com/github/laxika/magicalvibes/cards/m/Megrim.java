package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "157")
public class Megrim extends Card {

    public Megrim() {
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new DealDamageToDiscardingPlayerEffect(2));
    }
}
