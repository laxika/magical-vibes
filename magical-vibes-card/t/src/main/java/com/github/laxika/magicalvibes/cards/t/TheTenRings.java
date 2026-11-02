package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "251")
public class TheTenRings extends Card {

    public TheTenRings() {
        addStaticEffect(new SetControllerMaximumHandSizeEffect(10));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new CardsInHandAtMost(9),
                new DrawCardEffect(new Max(
                        new Fixed(0),
                        new Sum(new Fixed(10), new Scaled(new CardsInHand(CountScope.CONTROLLER), -1))))));
    }
}
