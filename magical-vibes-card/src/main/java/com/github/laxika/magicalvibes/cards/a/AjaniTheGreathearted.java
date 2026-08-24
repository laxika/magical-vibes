package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "184")
public class AjaniTheGreathearted extends Card {

    public AjaniTheGreathearted() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GainLifeEffect(3)),
                "+1: You gain 3 life."
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
                "\u22122: Put a +1/+1 counter on each creature you control and a loyalty counter on each other planeswalker you control."
        ));
    }
}
