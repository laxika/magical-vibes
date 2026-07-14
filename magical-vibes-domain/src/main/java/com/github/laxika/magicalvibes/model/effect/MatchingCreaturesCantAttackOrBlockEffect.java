package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static effect: while the source permanent is on the battlefield, any creature matching
 * {@code affectedPredicate} can't attack or block. The predicate is evaluated with the source
 * permanent's controller as the {@code sourceControllerId} (and the source card as
 * {@code sourceCardId}), so source-relative predicates such as
 * {@link com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate}
 * resolve "you"/"your opponents" from the source's perspective.
 * Examples: Kulrath Knight — "Creatures your opponents control with counters on them can't attack
 * or block." ({@code affectedPredicate = not-controlled-by-you AND has counters}); Light of Day —
 * "Black creatures can't attack or block." ({@code affectedPredicate = black-creature}).
 *
 * <p>The attack side is enforced in {@code CombatAttackService.isCantAttackDueToGlobalRestriction};
 * the block side in {@code GameQueryService} (block-legality).
 *
 * @param affectedPredicate which creatures can't attack or block (evaluated relative to the source)
 * @param description       short human-readable phrase for the restriction message
 */
public record MatchingCreaturesCantAttackOrBlockEffect(PermanentPredicate affectedPredicate,
                                                       String description) implements AttackOrBlockRestrictionEffect {

    @Override
    public PermanentPredicate globallyCantAttackOrBlock() {
        return affectedPredicate;
    }

    @Override
    public String restrictionDescription() {
        return description;
    }
}
