package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * When resolved, the targeted creature must block the source permanent this turn if able.
 * The sourcePermanentId is null in the card definition and gets snapshot at activation time
 * (activated abilities) or when the attacking creature triggers a granted "must block" ability
 * (see {@code CombatAttackService}).
 */
public record MustBlockSourceEffect(UUID sourcePermanentId) implements CardEffect {
    @Override public boolean canTargetPermanent() { return true; }

    /** Only creatures can be forced to block, so restrict targeted-trigger candidates to creatures. */
    @Override public PermanentPredicate targetPredicate() { return new PermanentIsCreaturePredicate(); }
}
