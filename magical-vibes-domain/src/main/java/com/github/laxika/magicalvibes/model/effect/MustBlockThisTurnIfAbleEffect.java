package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * When resolved, the targeted creature must be declared as a blocker this turn if it is able to
 * block any attacking creature (sets {@code Permanent.mustBlockThisTurnIfAble}, cleared at end of
 * turn). Unlike {@link MustBlockSourceEffect} (Provoke — must block one <em>specific</em> attacker),
 * this imposes the general "blocks this turn if able" requirement with no particular attacker in
 * mind. Enforced in {@code CombatBlockService.validateMustBlockIfAbleRequirements}.
 *
 * <p>Used by Nacatl Hunt-Pride ("{G}, {T}: Target creature blocks this turn if able.").
 */
public record MustBlockThisTurnIfAbleEffect() implements CardEffect {

    /**
     * Only creatures can be forced to block; the CREATURE category enforces that, and the predicate
     * preserves the creature restriction on targeted-trigger candidates.
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE, new PermanentIsCreaturePredicate());
    }
}
