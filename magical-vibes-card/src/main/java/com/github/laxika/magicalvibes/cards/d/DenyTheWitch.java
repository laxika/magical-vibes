package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "116")
public class DenyTheWitch extends Card {

    public DenyTheWitch() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL,
                        StackEntryType.ACTIVATED_ABILITY,
                        StackEntryType.TRIGGERED_ABILITY)),
                "Target must be a spell, activated ability, or triggered ability."
        ))
                .addEffect(EffectSlot.SPELL, new TargetSpellControllerLosesLifeEffect(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
