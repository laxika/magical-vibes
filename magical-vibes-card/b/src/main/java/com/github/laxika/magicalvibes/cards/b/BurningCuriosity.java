package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PutCounterCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;

@CardRegistration(set = "ECL", collectorNumber = "129")
public class BurningCuriosity extends Card {

    public BurningCuriosity() {
        addCastingCost(new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1, true));
        addSpellEffect(new ConditionalEffect(new PutCounterCostPaid(),
                        new ExileTopCardMayPlayThisTurnEffect(3, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)));
        addSpellEffect(new ConditionalEffect(new NotCondition(new PutCounterCostPaid()),
                        new ExileTopCardMayPlayThisTurnEffect(2, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)));
    }
}
