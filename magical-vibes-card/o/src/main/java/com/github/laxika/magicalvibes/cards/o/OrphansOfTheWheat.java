package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "22")
public class OrphansOfTheWheat extends Card {
    public OrphansOfTheWheat() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentCanBecomeTappedPredicate()))),
                        "any number of untapped creatures you control to tap"),
                new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)),
                new BoostSelfEffect(new AmountThisWay(StepKind.TAP), new AmountThisWay(StepKind.TAP))));
    }
}
