package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "269")
public class DomriCitySmasher extends Card {

    public DomriCitySmasher() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        BoostEffect.creaturesYouControl(1, 1),
                        new GrantKeywordEffect(Keyword.HASTE, Subjects.otherCreaturesYouControl())
                ),
                "+2: Creatures you control get +1/+1 and gain haste until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(DealDamageEffect.toAnyTarget(3)),
                "−3: Domri deals 3 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 3, new PermanentIsCreaturePredicate()),
                        new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl())
                ),
                "−8: Put three +1/+1 counters on each creature you control. Those creatures gain trample until end of turn."
        ));
    }
}
