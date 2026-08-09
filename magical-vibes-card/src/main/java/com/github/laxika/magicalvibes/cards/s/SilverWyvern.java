package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "43")
public class SilverWyvern extends Card {

    public SilverWyvern() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new ChangeTargetOfTargetSpellWithSingleTargetEffect(true)),
                "{U}: Change the target of target spell or ability that targets only Silver Wyvern. The new target must be a creature.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryHasTargetPredicate(),
                                new StackEntryIsSingleTargetPredicate(),
                                new StackEntryTargetsSourcePredicate()
                        )),
                        "Target must be a spell or ability with only Silver Wyvern as its target."
                )
        ));
    }
}
