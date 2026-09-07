package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PlusOnePlusOneCounterPutOnCreatureThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "247")
public class SigardianPaladin extends Card {

    public SigardianPaladin() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new PlusOnePlusOneCounterPutOnCreatureThisTurn(),
                new StaticBoostEffect(0, 0, Set.of(Keyword.TRAMPLE, Keyword.LIFELINK), GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{W}",
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.TRAMPLE, Keyword.LIFELINK), GrantScope.TARGET)),
                "{1}{G}{W}: Target creature you control with a +1/+1 counter on it gains trample and lifelink until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
                        )),
                        "Target must be a creature you control with a +1/+1 counter on it")));
    }
}
