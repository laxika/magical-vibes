package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect: prevent combat damage dealt by this creature to matching creatures,
 * then shuffle each such creature into its owner's library.
 */
public record PreventCombatDamageBySelfToCreaturesAndShuffleEffect(PermanentPredicate targetFilter)
        implements DamagePreventionBySelfEffect {

    public PreventCombatDamageBySelfToCreaturesAndShuffleEffect() {
        this(null);
    }

    @Override
    public boolean combatOnly() {
        return true;
    }

    @Override
    public boolean shuffleTargetIntoOwnersLibrary() {
        return true;
    }
}
