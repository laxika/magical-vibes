package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1552")
public class DeflectingSwat extends Card {

    public DeflectingSwat() {
        addCastingOption(new AlternateHandCast(
                List.of(), new ControlsPermanent(new PermanentIsCommanderPredicate()), false));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.TRIGGERED_ABILITY,
                        StackEntryType.ACTIVATED_ABILITY,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL)),
                "Target must be a spell or ability."))
                .addEffect(EffectSlot.SPELL,
                        new MayEffect(new ChooseNewTargetsForTargetSpellEffect(), "Choose new targets?"));
    }
}
