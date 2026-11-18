package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the target creature protection from creatures controlled by its opponents until end of turn. The
 * resolution grants this record to the creature as an ability through a floating {@link GrantEffectEffect}
 * (layer 6, CR 613.1f); "its opponents" are its controller's opponents at each check.
 */
public record GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
