package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

/**
 * Redirects all damage dealt by the targeted sorcery to that sorcery's controller for the rest of
 * the turn.
 */
public record RedirectAllDamageFromTargetSorceryToControllerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spells(new StackEntryTypeInPredicate(
                Set.of(StackEntryType.SORCERY_SPELL))));
    }
}
