package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "180")
public class MinasTirithGarrison extends Card {

    public MinasTirithGarrison() {
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new CardsInHand(CountScope.CONTROLLER), new Fixed(5)));
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                                new PermanentAllOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                                        new PermanentCanBecomeTappedPredicate()))),
                        "any number of untapped Humans you control to tap"),
                new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)),
                new DrawCardEffect(new AmountThisWay(StepKind.TAP), Subjects.you())));
    }
}
