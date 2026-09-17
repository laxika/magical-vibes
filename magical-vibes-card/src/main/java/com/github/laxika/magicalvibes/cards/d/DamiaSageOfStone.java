package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;

@CardRegistration(set = "CMD", collectorNumber = "191")
public class DamiaSageOfStone extends Card {

    public DamiaSageOfStone() {
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());

        // At the beginning of your upkeep, if you have fewer than seven cards in hand, draw cards
        // equal to the difference.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new CardsInHandAtMost(6),
                new DrawCardEffect(new Sum(
                        new Fixed(7),
                        new Scaled(new CardsInHand(CountScope.CONTROLLER), -1)))));
    }
}
