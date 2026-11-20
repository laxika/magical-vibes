package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ULG", collectorNumber = "83")
public class LastDitchEffort extends Card {

    public LastDitchEffort() {
        addSpellEffect(SacrificePermanentsEffect.anyNumber(new PermanentIsCreaturePredicate(), Subjects.you()));
        addSpellEffect(new DealDamageToAnyTargetEffect(new AmountThisWay(StepKind.SACRIFICE)));
    }
}
