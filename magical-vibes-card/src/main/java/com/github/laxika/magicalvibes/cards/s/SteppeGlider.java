package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "36")
public class SteppeGlider extends Card {

    public SteppeGlider() {
        var creatureWithPlusOnePlusOneCounter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.FLYING, Keyword.VIGILANCE),
                        GrantScope.TARGET,
                        creatureWithPlusOnePlusOneCounter
                )),
                "{1}{W}: Target creature with a +1/+1 counter on it gains flying and vigilance until end of turn.",
                new PermanentPredicateTargetFilter(
                        creatureWithPlusOnePlusOneCounter,
                        "Target must be a creature with a +1/+1 counter"
                )
        ));
    }
}
