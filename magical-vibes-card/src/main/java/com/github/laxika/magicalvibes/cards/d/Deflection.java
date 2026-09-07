package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "81")
@CardRegistration(set = "8ED", collectorNumber = "74")
@CardRegistration(set = "7ED", collectorNumber = "69")
@CardRegistration(set = "6ED", collectorNumber = "63")
@CardRegistration(set = "ICE", collectorNumber = "65")
public class Deflection extends Card {

    public Deflection() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.CREATURE_SPELL,
                                StackEntryType.ENCHANTMENT_SPELL,
                                StackEntryType.SORCERY_SPELL,
                                StackEntryType.INSTANT_SPELL,
                                StackEntryType.ARTIFACT_SPELL,
                                StackEntryType.PLANESWALKER_SPELL,
                                StackEntryType.BATTLE_SPELL)),
                        new StackEntryIsSingleTargetPredicate())),
                "Target spell must have a single target."
        )).addEffect(EffectSlot.SPELL, new ChangeTargetOfTargetSpellWithSingleTargetEffect());
    }
}
