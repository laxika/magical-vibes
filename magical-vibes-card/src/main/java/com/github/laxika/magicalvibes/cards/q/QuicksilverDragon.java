package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "103")
public class QuicksilverDragon extends Card {

    public QuicksilverDragon() {
        addMorph("{4}{U}");
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(ChangeTargetOfTargetSpellWithSingleTargetEffect.sourceCreatureTargetsOnly()),
                "{U}: If target spell has only one target and that target is this creature, change that spell's target to another creature.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.CREATURE_SPELL,
                                StackEntryType.ENCHANTMENT_SPELL,
                                StackEntryType.SORCERY_SPELL,
                                StackEntryType.INSTANT_SPELL,
                                StackEntryType.ARTIFACT_SPELL,
                                StackEntryType.PLANESWALKER_SPELL,
                                StackEntryType.BATTLE_SPELL
                        )),
                        "Target must be a spell."
                )
        ));
    }
}
