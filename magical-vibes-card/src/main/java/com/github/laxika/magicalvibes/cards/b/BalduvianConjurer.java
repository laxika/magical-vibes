package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "58")
public class BalduvianConjurer extends Card {

    public BalduvianConjurer() {
        // {T}: Target snow land becomes a 2/2 creature until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AnimatePermanentsEffect(
                        2, 2, List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN)),
                "{T}: Target snow land becomes a 2/2 creature until end of turn. It's still a land.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)
                        )),
                        "Target must be a snow land")));
    }
}
