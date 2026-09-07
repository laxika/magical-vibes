package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "59")
public class LouisoixsSacrifice extends Card {

    public LouisoixsSacrifice() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAnyOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.ACTIVATED_ABILITY,
                                StackEntryType.TRIGGERED_ABILITY)),
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.ENCHANTMENT_SPELL,
                                StackEntryType.SORCERY_SPELL,
                                StackEntryType.INSTANT_SPELL,
                                StackEntryType.ARTIFACT_SPELL,
                                StackEntryType.PLANESWALKER_SPELL,
                                StackEntryType.BATTLE_SPELL)))),
                "Target must be an activated ability, triggered ability, or noncreature spell."
        )).addEffect(EffectSlot.SPELL, new SacrificePermanentOrPayManaCost(
                "{2}",
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                "a legendary creature"))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
