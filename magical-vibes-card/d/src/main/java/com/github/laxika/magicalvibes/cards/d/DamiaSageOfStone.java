package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMD", collectorNumber = "191")
@CardRegistration(set = "CM2", collectorNumber = "2")
public class DamiaSageOfStone extends Card {

    public DamiaSageOfStone() {
        addStaticEffect(new SkipDrawStepEffect());

        // At the beginning of your upkeep, if you have fewer than seven cards in hand, draw cards
        // equal to the difference.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new CardsInHandAtMost(6),
                new DrawCardEffect(new Sum(
                        new Fixed(7),
                        new Scaled(new CardsInHand(CountScope.CONTROLLER), -1)))));
    }
}
