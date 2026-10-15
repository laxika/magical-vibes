package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "61")
public class FendeepSummoner extends Card {

    public FendeepSummoner() {
        // {T}: Up to two target Swamps each become 3/5 Treefolk Warrior creatures
        // in addition to their other types until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, // requiresTap
                null, // no mana cost
                List.of(new AnimatePermanentsEffect(
                        3, 5, List.of(CardSubtype.TREEFOLK, CardSubtype.WARRIOR), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN)),
                "{T}: Up to two target Swamps each become 3/5 Treefolk Warrior creatures "
                        + "in addition to their other types until end of turn.",
                List.of(
                        new PermanentPredicateTargetFilter(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), "Target must be a Swamp"),
                        new PermanentPredicateTargetFilter(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), "Target must be a Swamp")
                ),
                0,  // minTargets (up to two)
                2   // maxTargets
        ));
    }
}
