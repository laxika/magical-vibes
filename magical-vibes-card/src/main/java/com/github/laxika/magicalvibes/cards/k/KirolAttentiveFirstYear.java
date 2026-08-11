package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyTargetTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "231")
public class KirolAttentiveFirstYear extends Card {

    public KirolAttentiveFirstYear() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        new CopyTargetTriggeredAbilityEffect()
                ),
                "Tap two untapped creatures you control: Copy target triggered ability you control. You may choose new targets for the copy. Activate only once each turn.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryControlledByPredicate()
                        )),
                        "Target must be a triggered ability you control."
                ),
                null,
                1,
                null
        ));
    }
}
