package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "105")
@CardRegistration(set = "ME3", collectorNumber = "70")
public class Hellfire extends Card {

    public Hellfire() {
        addSpellEffect(new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                ))));
        addSpellEffect(DealDamageEffect.to(Subjects.you(),
                new Sum(AmountThisWay.died(StepKind.DESTROY), new Fixed(3))));
    }
}
