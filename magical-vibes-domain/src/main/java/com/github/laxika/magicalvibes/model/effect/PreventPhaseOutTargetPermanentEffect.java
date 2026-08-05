package com.github.laxika.magicalvibes.model.effect;

/**
 * "Until your next upkeep, target permanent can't phase out." (Spatial Binding). Marks the targeted
 * permanent with the resolving controller's id in {@code Permanent.cantPhaseOutUntilUpkeepOf}; while
 * marked it is skipped by every phasing pass — both the untap step's turn-based action (CR 502.1)
 * and one-shot effects that make a permanent phase out. Auras and Equipment attached to it still
 * phase out indirectly (CR 702.26g) only when their host actually phases out, which a marked host
 * never does.
 *
 * <p>The restriction ends at the beginning of the marking player's next upkeep, one step after the
 * untap step, so a permanent protected on turn N is still protected during the protector's own
 * untap step on turn N+1.
 */
public record PreventPhaseOutTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
