package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "218")
public class MoldgrafScavenger extends Card {

    public MoldgrafScavenger() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(),
                new StaticBoostEffect(3, 0, Set.of(), GrantScope.SELF)));
    }
}
