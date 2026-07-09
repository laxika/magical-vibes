package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "196")
public class WildRicochet extends Card {

    public WildRicochet() {
        // "You may choose new targets for target instant or sorcery spell. Then copy that spell.
        //  You may choose new targets for the copy."
        // Both effects act on the single targeted spell. The retarget-original effect resolves first
        // so the copy (created by CopySpellEffect, which itself offers "new targets for the copy")
        // inherits any changed targets.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        ))
                .addEffect(EffectSlot.SPELL, new MayEffect(new ChooseNewTargetsForTargetSpellEffect(),
                        "Choose new targets for the spell?"))
                .addEffect(EffectSlot.SPELL, new CopySpellEffect());
    }
}
