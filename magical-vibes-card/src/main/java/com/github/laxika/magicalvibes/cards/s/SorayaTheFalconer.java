package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "18")
public class SorayaTheFalconer extends Card {

    private static final PermanentPredicate BIRD_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.BIRD))
    ));

    public SorayaTheFalconer() {
        // Bird creatures get +1/+1. (All players' Birds, not just yours.)
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.BIRD))));

        // {1}{W}: Target Bird creature gains banding until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantKeywordEffect(Keyword.BANDING, GrantScope.TARGET, BIRD_CREATURE)),
                "{1}{W}: Target Bird creature gains banding until end of turn.",
                new PermanentPredicateTargetFilter(BIRD_CREATURE, "Target must be a Bird creature")
        ));
    }
}
