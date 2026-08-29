package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "M21", collectorNumber = "26")
@CardRegistration(set = "WAR", collectorNumber = "22")
public class MakeshiftBattalion extends Card {

    public MakeshiftBattalion() {
        // Battalion — Whenever this creature and at least two other creatures attack,
        // put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(3),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
