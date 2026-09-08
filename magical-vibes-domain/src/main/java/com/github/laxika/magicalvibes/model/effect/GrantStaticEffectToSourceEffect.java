package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the source permanent carry a static effect for the specified duration.
 *
 * <p>The effect is resolved once and then evaluated by the continuous-effect layer system. This
 * is useful when a resolving ability makes an Aura gain a static ability that follows the Aura's
 * current attachment.</p>
 */
public record GrantStaticEffectToSourceEffect(CardEffect staticEffect, EffectDuration duration)
        implements CardEffect {

    public GrantStaticEffectToSourceEffect(CardEffect staticEffect) {
        this(staticEffect, EffectDuration.WHILE_SOURCE_REMAINS);
    }
}
