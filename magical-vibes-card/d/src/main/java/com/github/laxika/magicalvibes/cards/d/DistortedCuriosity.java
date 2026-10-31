package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "ONE", collectorNumber = "46")
public class DistortedCuriosity extends Card {

    public DistortedCuriosity() {
        addStaticEffect(new ConditionalEffect(
                new OpponentPoisoned(3),
                new ReduceOwnCastCostEffect(new Fixed(2))));
        addSpellEffect(new DrawCardEffect(2));
    }
}
