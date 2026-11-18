package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Target creature loses all '[static ability]' abilities until end of turn" (Tolaria, Shelkin Brownie): a layer-6
 * ability removal (CR 613.1f). As it resolves it becomes a floating effect on the target with this record as its
 * payload; the abilities of that kind it has then are removed, one granted later stays (CR 613.7).
 *
 * @param effectType the concrete static effect type to suppress
 */
public record SuppressStaticEffectOnTargetUntilEndOfTurnEffect(
        Class<? extends CardEffect> effectType
) implements CardEffect {

    public SuppressStaticEffectOnTargetUntilEndOfTurnEffect {
        Objects.requireNonNull(effectType, "effectType");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
