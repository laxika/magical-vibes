package com.github.laxika.magicalvibes.model.effect;

/**
 * The source permanent's controller sacrifices it and then draws {@code cards} cards — Soul
 * Ransom's "This Aura's controller sacrifices it, then draws two cards."
 *
 * <p>Unlike {@link SacrificeSelfThenEffect} paired with {@link DrawCardEffect}, both halves act on
 * the <em>source permanent's</em> controller rather than the stack entry's controller. The ability
 * carrying this effect is activated by an opponent, so the two differ.
 */
public record SacrificeSelfAndControllerDrawsEffect(int cards) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        // Implicitly acts on its own source permanent, exactly like SacrificeSelfEffect.
        return new TargetSpec(null, false, null, true, 1);
    }
}
