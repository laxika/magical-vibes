package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marks permanent(s) so they don't untap during their controller's next untap step (a one-shot
 * "skip next untap", modelled by incrementing {@code Permanent.skipUntapCount}, which the untap step
 * reads and decrements). The {@link TapUntapScope} selects which permanent(s) are affected; the
 * optional {@link PermanentPredicate} narrows the scanned scopes ({@link TapUntapScope#ALL_CREATURES},
 * {@link TapUntapScope#TARGET_PLAYERS_PERMANENTS}). The {@link TapUntapScope#SELF} scope keeps the
 * source permanent itself tapped through its next untap step (e.g. an attack trigger on Lead Golem).
 *
 * <p>Replaces the former {@code SkipNextUntapOnTargetEffect}, {@code SkipNextUntapAllAttackingCreaturesEffect}
 * and {@code SkipNextUntapPermanentsOfTargetPlayerEffect}.
 *
 * <p>Also a {@link CombatOpponentReferencingEffect}: when placed on the {@code ON_BLOCK} slot of an
 * attached permanent (aura/equipment) with {@link TapUntapScope#TARGET}, {@code CombatTriggerService}
 * auto-targets the equipped/enchanted creature's combat opponent (the blocked attacker) — e.g. Shield
 * of the Righteous. On a creature's own {@code ON_BLOCK} trigger (Wall of Frost) the same auto-target
 * is applied by {@code CombatBlockService}. Other scopes ignore the referenced target.
 *
 * @param scope  which permanent(s) to keep tapped through their next untap step
 * @param filter optional predicate narrowing the scanned scopes (null = no restriction)
 */
public record SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter)
        implements CardEffect, CombatOpponentReferencingEffect {

    public SkipNextUntapEffect(TapUntapScope scope) {
        this(scope, null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (scope == TapUntapScope.TARGET) {
            return TargetSpec.benign(TargetCategory.PERMANENT);
        }
        if (scope == TapUntapScope.TARGET_PLAYERS_PERMANENTS) {
            return TargetSpec.benign(TargetCategory.PLAYER);
        }
        return TargetSpec.NONE;
    }
}
