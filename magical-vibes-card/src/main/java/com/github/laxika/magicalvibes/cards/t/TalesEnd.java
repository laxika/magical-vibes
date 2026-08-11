package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySupertypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "77")
public class TalesEnd extends Card {

    public TalesEnd() {
        StackEntryTypeInPredicate abilityTypes = new StackEntryTypeInPredicate(Set.of(
                StackEntryType.ACTIVATED_ABILITY,
                StackEntryType.TRIGGERED_ABILITY));
        StackEntryAllOfPredicate legendarySpell = new StackEntryAllOfPredicate(List.of(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL)),
                new StackEntrySupertypeInPredicate(Set.of(CardSupertype.LEGENDARY))));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAnyOfPredicate(List.of(abilityTypes, legendarySpell)),
                "Target must be an activated or triggered ability, or a legendary spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
