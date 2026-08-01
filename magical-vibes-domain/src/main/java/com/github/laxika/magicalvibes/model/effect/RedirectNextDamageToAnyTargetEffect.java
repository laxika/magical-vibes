package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next {@code amount} damage that would be dealt to this creature this turn is dealt to any
 * target instead." The redirect destination (creature, planeswalker, or player) is the ability's
 * target; the protected creature is this ability's source permanent. Applies to the next
 * {@code amount} damage from any source (combat or noncombat), then the shield is consumed.
 * Used by Zhalfirin Crusader. The any-target generalisation of
 * {@link RedirectNextDamageToTargetCreatureEffect}.
 */
public record RedirectNextDamageToAnyTargetEffect(int amount) implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }
}
