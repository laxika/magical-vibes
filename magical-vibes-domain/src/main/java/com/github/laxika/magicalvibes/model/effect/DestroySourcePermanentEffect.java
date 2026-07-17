package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy the source permanent (identified by sourcePermanentId on the stack entry).
 * When {@code cannotBeRegenerated} is true, regeneration shields are ignored (Aether Storm).
 */
public record DestroySourcePermanentEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroySourcePermanentEffect() {
        this(false);
    }
}
