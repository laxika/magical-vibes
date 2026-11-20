package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "6")
@CardRegistration(set = "BLC", collectorNumber = "42")
public class MrFoxglove extends Card {

    public MrFoxglove() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new DrawCardEffect(new Max(
                        new Fixed(0),
                        new Sum(
                                new CardsInHand(CountScope.DEFENDING_PLAYER),
                                new Scaled(new CardsInHand(CountScope.CONTROLLER), -1)))),
                new ConditionalEffect(new NotCondition(HappenedThisWay.drew()), new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                        "Put a creature card from your hand onto the battlefield?"))));
    }
}
