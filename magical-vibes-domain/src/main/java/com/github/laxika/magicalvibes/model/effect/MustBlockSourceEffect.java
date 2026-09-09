package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * When resolved, the targeted creature must block the source permanent this turn if able.
 * The sourcePermanentId is null in the card definition and gets snapshot at activation time
 * (activated abilities) or when the attacking creature triggers a granted "must block" ability
 * (see {@code CombatAttackService}).
 */
public record MustBlockSourceEffect(UUID sourcePermanentId, PermanentPredicate targetRestriction)
        implements CardEffect {

    public MustBlockSourceEffect(UUID sourcePermanentId) {
        this(sourcePermanentId, null);
    }

    /**
     * Only creatures can be forced to block; the CREATURE category enforces that, and the predicate
     * preserves the creature restriction on targeted-trigger candidates (carried on the spec predicate).
     */
    @Override
    public TargetSpec targetSpec() {
        PermanentPredicate restriction = targetRestriction == null
                ? new PermanentIsCreaturePredicate()
                : new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        targetRestriction));
        return TargetSpec.benign(TargetPredicates.creature(), restriction);
    }
}
