package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot delayed trigger: "When you next cast an instant or sorcery spell this turn,
 * copy that spell. You may choose new targets for the copy."
 * <p>
 * Tracked via {@code GameData.pendingNextInstantSorceryCopyThisTurnCount}: consumed by the next
 * instant or sorcery the controller casts and cleared at end of turn. This is the turn-scoped
 * sibling of {@code pendingNextInstantSorceryCopyCount}, which backs Primal Wellspring's
 * mana-linked copy ({@link AwardAnyColorManaEffect} with
 * {@link ManaSpendRestriction#INSTANT_SORCERY_COPY}) and is instead cleared
 * whenever mana pools drain.
 * <p>
 * Used by Chandra, the Firebrand's −2 ability.
 */
public record CopyNextInstantOrSorceryCastThisTurnEffect() implements CardEffect {
}
