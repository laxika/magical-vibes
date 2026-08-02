package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AjaniSteadfastEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "1")
public class AjaniSteadfast extends Card {

    public AjaniSteadfast() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(
                                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE, Keyword.LIFELINK),
                                GrantScope.TARGET)
                ),
                "+1: Until end of turn, up to one target creature gets +1/+1 and gains first strike, vigilance, and lifelink.",
                TargetFilters.creature(),
                +1, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.LOYALTY, 1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsPlaneswalkerPredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                                )))
                ),
                "-2: Put a +1/+1 counter on each creature you control and a loyalty counter on each other planeswalker you control."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new AjaniSteadfastEmblemEffect()),
                "-7: You get an emblem with \"If a source would deal damage to you or a planeswalker you control, prevent all but 1 of that damage.\""
        ));
    }
}
