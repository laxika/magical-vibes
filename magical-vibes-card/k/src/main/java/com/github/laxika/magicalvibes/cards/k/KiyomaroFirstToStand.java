package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "18")
public class KiyomaroFirstToStand extends Card {

    public KiyomaroFirstToStand() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));
        addStaticEffect(new ConditionalEffect(
                new CardsInHandAtLeast(4),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));
        addTrigger(Triggers.dealsDamage(),
                TriggerOptions.DEFAULT.withInterveningIf(new CardsInHandAtLeast(7)),
                new GainLifeEffect(7));
    }
}
