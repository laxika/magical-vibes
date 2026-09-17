package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "All [permanents] you control phase out." (Taniwha). One-shot, non-targeting mass phasing: every
 * permanent matching {@code filter} — restricted to the source controller's battlefield when
 * {@code controllerOnly} is true — moves into {@code GameData.phasedOutPermanents} together with
 * anything attached to it (CR 702.26g) and is removed from combat (CR 702.26b).
 *
 * <p>Because they phased out directly, they phase in during their controller's next untap step even
 * without the phasing keyword (CR 702.26a).
 *
 * <p>Mass sibling of {@link PhaseOutEffect}, which phases out one named permanent.
 *
 * @param filter which permanents phase out
 * @param scope which battlefield(s) to consider
 */
public record PhaseOutPermanentsEffect(PermanentPredicate filter, PhaseOutScope scope)
        implements CardEffect {

    /** Compatibility constructor for the original controller/all scopes. */
    public PhaseOutPermanentsEffect(PermanentPredicate filter, boolean controllerOnly) {
        this(filter, controllerOnly ? PhaseOutScope.CONTROLLER : PhaseOutScope.ALL);
    }

    /** "Each creature target player controls phases out." */
    public static PhaseOutPermanentsEffect targetPlayer(PermanentPredicate filter) {
        return new PhaseOutPermanentsEffect(filter, PhaseOutScope.TARGET_PLAYER);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == PhaseOutScope.TARGET_PLAYER
                ? TargetSpec.harmful(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
