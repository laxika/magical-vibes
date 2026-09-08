package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot delayed trigger: "When you next cast an instant or sorcery spell this turn,
 * copy that spell. You may choose new targets for the copy."
 * <p>
 * Unrestricted triggers use {@code GameData.pendingNextInstantSorceryCopyThisTurnCount}; limited
 * triggers use the per-player max-mana-value list. Both are consumed by the next matching spell
 * and cleared at end of turn. This is the turn-scoped
 * sibling of {@code pendingNextInstantSorceryCopyCount}, which backs Primal Wellspring's
 * mana-linked copy ({@link AwardAnyColorManaEffect} with
 * {@link ManaSpendRestriction#INSTANT_SORCERY_COPY}) and is instead cleared
 * whenever mana pools drain.
 * <p>
 * A non-null {@code maxManaValue} limits the trigger to spells with that mana value or less.
 * <p>
 * Used by Chandra, the Firebrand's −2 ability.
 */
public record CopyNextInstantOrSorceryCastThisTurnEffect(Integer maxManaValue) implements CardEffect {

    public CopyNextInstantOrSorceryCastThisTurnEffect() {
        this(null);
    }
}
