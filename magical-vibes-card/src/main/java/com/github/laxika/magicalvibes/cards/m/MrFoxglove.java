package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardThenIfNoCardsDrawnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BLC", collectorNumber = "6")
@CardRegistration(set = "BLC", collectorNumber = "42")
public class MrFoxglove extends Card {

    public MrFoxglove() {
        addEffect(EffectSlot.ON_ATTACK, new DrawCardThenIfNoCardsDrawnEffect(
                new Max(
                        new Fixed(0),
                        new Sum(
                                new CardsInHand(CountScope.DEFENDING_PLAYER),
                                new Scaled(new CardsInHand(CountScope.CONTROLLER), -1))),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                        "Put a creature card from your hand onto the battlefield?")));
    }
}
