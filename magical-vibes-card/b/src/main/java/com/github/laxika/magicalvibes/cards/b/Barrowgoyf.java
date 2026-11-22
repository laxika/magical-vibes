package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AA2", collectorNumber = "7")
@CardRegistration(set = "M3C", collectorNumber = "50")
@CardRegistration(set = "M3C", collectorNumber = "102")
public class Barrowgoyf extends Card {

    public Barrowgoyf() {
        CardTypesAmongCardsInGraveyard cardTypes =
                new CardTypesAmongCardsInGraveyard(CountScope.ANY_PLAYER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                cardTypes, new Sum(cardTypes, new Fixed(1))));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                SequenceEffect.of(
                        new MillEffect(new EventValue(), Subjects.you()),
                        ChooseCardEffect.upTo(
                                Subjects.cardsThisWay(StepKind.MILL, new CardTypePredicate(CardType.CREATURE)), 1,
                                "a creature card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                "Mill that many cards?"));
    }
}
