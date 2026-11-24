package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "16")
public class DevoutInvocation extends Card {
    public DevoutInvocation() {
        addSpellEffect(ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentCanBecomeTappedPredicate()))),
                "any number of untapped creatures you control to tap"));
        addSpellEffect(new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)));
        addSpellEffect(new CreateTokenEffect(new AmountThisWay(StepKind.TAP), "Angel", 4, 4, CardColor.WHITE,
                List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of()));
    }
}
