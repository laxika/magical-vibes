package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed one-shot trigger that copies the next <em>red</em> instant or sorcery spell
 * the controller casts, letting them choose new targets for the copy.
 * <p>
 * Pairs with an {@link AwardManaEffect} in the same mana ability to model the mana-linked
 * "When that mana is spent to cast a red instant or sorcery spell, copy that spell" rider of
 * Pyromancer's Goggles. The engine approximates the mana link the same way Primal Wellspring's
 * {@link AwardAnyColorManaWithInstantSorceryCopyEffect} does: the pending copy is tracked in
 * {@code GameData.pendingNextRedInstantSorceryCopyCount} and cleared when mana pools drain, so it
 * only applies while the produced mana could still be spent.
 */
public record RegisterNextRedInstantSorceryCopyEffect() implements CardEffect {
}
