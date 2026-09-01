package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Suppresses one printed static effect on the targeted permanent until end of turn.
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
