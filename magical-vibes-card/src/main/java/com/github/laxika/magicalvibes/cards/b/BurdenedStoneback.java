package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "8")
public class BurdenedStoneback extends Card {

    public BurdenedStoneback() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSourceEffect(-1, -1, 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new RemoveCounterFromSourceCost(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantKeywordEffect.Scope.TARGET)
                ),
                "{1}{W}, Remove a counter from this creature: Target creature gains indestructible until end of turn. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
