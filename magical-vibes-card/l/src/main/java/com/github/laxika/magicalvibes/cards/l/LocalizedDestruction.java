package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostAmountPredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "41")
@CardRegistration(set = "M3C", collectorNumber = "93")
public class LocalizedDestruction extends Card {

    public LocalizedDestruction() {
        addSpellEffect(new EnergyCountersEffect(1));
        addSpellEffect(EachPlayerPaysAnyManaEffect.byYou(Resource.ENERGY));
        addSpellEffect(new ConditionalEffect(new HappenedThisWay(StepKind.PAY_ENERGY),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.controlledBy(Subjects.you(),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastAmountPredicate(AmountThisWay.energyPaid()),
                                new PermanentPowerAtMostAmountPredicate(AmountThisWay.energyPaid())))))));
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
