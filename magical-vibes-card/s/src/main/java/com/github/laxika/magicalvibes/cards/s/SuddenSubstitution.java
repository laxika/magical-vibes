package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfPermanentAndSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "C19", collectorNumber = "11")
public class SuddenSubstitution extends Card {

    public SuddenSubstitution() {
        SpellTarget spell = target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL)),
                "Target must be a noncreature spell."));
        target(TargetFilters.creature())
                .addSpellEffect(ExchangeControlOfPermanentAndSpellEffect.targetCreatureAndTargetSpell());
        spell.addSpellEffect(new MayEffect(ChooseNewTargetsForTargetSpellEffect.ifItHasTargets(Subjects.targetSpell()),
                "Choose new targets for the spell?", null, MayChoicePlayer.TARGET_SPELL_CONTROLLER));
    }
}
