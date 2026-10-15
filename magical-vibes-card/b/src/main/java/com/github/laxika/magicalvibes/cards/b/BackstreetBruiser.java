package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCreatureCounterCountAtLeast;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "SNC", collectorNumber = "35")
public class BackstreetBruiser extends Card {

    public BackstreetBruiser() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlledCreatureCounterCountAtLeast(2),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
