package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * "Target spell or permanent becomes {@link #color}." A one-shot spell effect that sets the target's
 * color indefinitely (CR 105.3 / CR 611.2b — no stated duration, so the continuous effect lasts as
 * long as the affected object exists), replacing all its previous colors (layer 5 setter). Used by the
 * "lace" instants (Purelace → white).
 *
 * <p>Like {@link ChangeColorTextEffect} (Glamerdye) this targets a spell OR a permanent: the permanent
 * target is described by {@link #targetSpec()} ({@code PERMANENT}); the spell capability is exposed
 * through {@code EffectResolution.targetsSpellOnStack(effect)} and validated on the stack path. A color
 * set on a permanent spell carries onto the permanent it resolves into (CR 613.7).
 */
public record SetTargetColorEffect(CardColor color) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
