package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next {@code amount} damage that would be dealt to this creature this turn is dealt to target
 * creature instead." The target creature (the redirect destination) is the ability's target; the
 * protected creature is this ability's source permanent. Applies to the next {@code amount} damage
 * from any source (combat or noncombat), then the shield is consumed. Used by Zealous Inquisitor.
 */
public record RedirectNextDamageToTargetCreatureEffect(int amount) implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
