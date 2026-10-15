package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PutCounterCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;

@CardRegistration(set = "ECL", collectorNumber = "129")
public class BurningCuriosity extends Card {

    public BurningCuriosity() {
        addEffect(EffectSlot.SPELL,
                new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1, true));
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new PutCounterCostPaid(),
                        new ExileTopCardsMayPlayUntilNextTurnEffect(3)));
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new NotCondition(new PutCounterCostPaid()),
                        new ExileTopCardsMayPlayUntilNextTurnEffect(2)));
    }
}
