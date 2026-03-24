package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds one mana of any color to the controller's mana pool and registers a delayed trigger
 * that copies the next instant or sorcery spell the controller casts (one-shot).
 * <p>
 * Used by Primal Wellspring's mana ability:
 * "{T}: Add one mana of any color. When that mana is spent to cast an instant or sorcery spell,
 * copy that spell and you may choose new targets for the copy."
 * <p>
 * The delayed trigger is tracked via {@code GameData.pendingNextInstantSorceryCopyCount}
 * and cleared when mana pools drain (step/phase transition).
 */
public record AwardAnyColorManaWithInstantSorceryCopyEffect(int amount) implements ManaProducingEffect {

    public AwardAnyColorManaWithInstantSorceryCopyEffect() {
        this(1);
    }
}
