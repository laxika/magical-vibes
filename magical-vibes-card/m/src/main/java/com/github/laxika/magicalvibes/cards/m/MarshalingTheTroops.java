package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "141")
public class MarshalingTheTroops extends Card {
    public MarshalingTheTroops() {
        addSpellEffect(ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentCanBecomeTappedPredicate()))),
                "any number of untapped creatures you control to tap"));
        addSpellEffect(new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)));
        addSpellEffect(new GainLifeEffect(new Scaled(new AmountThisWay(StepKind.TAP), 4)));
    }
}
