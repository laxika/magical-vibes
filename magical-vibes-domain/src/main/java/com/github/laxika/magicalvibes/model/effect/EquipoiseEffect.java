package com.github.laxika.magicalvibes.model.effect;

/**
 * Equipoise upkeep trigger: for each land the target player controls in excess of the number the
 * controller controls, the controller chooses a land that player controls, then those permanents
 * phase out; the same process repeats for artifacts, then creatures.
 *
 * <p>Passes run in order (lands → artifacts → creatures). Permanents phased out in an earlier pass
 * no longer exist for later counts (Gatherer 2009-10-01: an artifact creature chosen during the
 * artifact pass reduces the creature count for the creature pass). Driven by
 * {@code EquipoiseSupport}.
 */
public record EquipoiseEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
