package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.UUID;

/**
 * When resolved, the targeted creature must block the source permanent this turn if able.
 * The sourcePermanentId is null in the card definition and gets snapshot at activation time
 * (activated abilities) or when the attacking creature triggers a granted "must block" ability
 * (see {@code CombatAttackService}).
 */
public record MustBlockSourceEffect(UUID sourcePermanentId) implements CardEffect {
    /**
     * Only creatures can be forced to block; the CREATURE category enforces that, and the predicate
     * preserves the creature restriction on targeted-trigger candidates (exposed via targetPredicate()).
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE, new PermanentIsCreaturePredicate());
    }
}
