package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the source permanent carry a static effect for the specified duration: "this Aura gains '[ability]'" with no
 * stated duration lasts until the end of the game (CR 611.2a), so for as long as the permanent stays on the battlefield
 * (CR 400.7), and is the default.
 *
 * <p>The effect is resolved once and then evaluated by the continuous-effect layer system. This
 * is useful when a resolving ability makes an Aura gain a static ability that follows the Aura's
 * current attachment.</p>
 */
public record GrantStaticEffectToSourceEffect(CardEffect staticEffect, Duration duration)
        implements CardEffect {

    public GrantStaticEffectToSourceEffect(CardEffect staticEffect) {
        this(staticEffect, Duration.PERMANENT);
    }
}
