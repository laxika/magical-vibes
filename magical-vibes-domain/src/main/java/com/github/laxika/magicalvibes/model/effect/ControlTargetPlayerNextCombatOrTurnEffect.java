package com.github.laxika.magicalvibes.model.effect;

/**
 * Controls target player during their next combat phase, or during their next turn when the
 * spell's optional waterbend cost was paid.
 */
public record ControlTargetPlayerNextCombatOrTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
