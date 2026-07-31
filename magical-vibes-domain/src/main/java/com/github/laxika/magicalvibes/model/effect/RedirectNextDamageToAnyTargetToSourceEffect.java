package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next {@code amount} damage that would be dealt to target creature, planeswalker, or player
 * this turn is dealt to this creature instead." The protected object is the ability's target and the
 * redirect destination is the ability's own source permanent. Applies to the next {@code amount}
 * damage from any source (combat or noncombat), then the shield is consumed.
 *
 * <p>The any-target generalisation of {@link RedirectNextDamageToTargetCreatureToSourceEffect}
 * (which is narrowed to a white creature you control for Hazduhr the Abbot). Used by Martyrdom.</p>
 */
public record RedirectNextDamageToAnyTargetToSourceEffect(int amount) implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.ANY_TARGET);
    }
}
