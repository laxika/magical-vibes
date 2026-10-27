package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Registers a one-shot delayed trigger: "When you next cast an instant or sorcery spell this turn,
 * copy that spell. You may choose new targets for the copy."
 * <p>
 * Unrestricted triggers use {@code GameData.pendingNextInstantSorceryCopyThisTurnCount}; limited
 * triggers use the per-player max-mana-value list. Both are consumed by the next matching spell
 * and cleared at end of turn. Unlike Primal Wellspring's mana-linked copy
 * ({@link AwardAnyColorManaEffect} with {@link ManaSpendRestriction#INSTANT_SORCERY_COPY}), which
 * rides on the produced mana itself, this trigger isn't tied to any mana.
 * <p>
 * A non-null {@code maxManaValue} limits the trigger to spells with that mana value or less.
 * A non-null {@code dynamicCopyCount} registers a count evaluated when the next spell is cast.
 * <p>
 * Used by Chandra, the Firebrand's −2 ability.
 */
public record CopyNextInstantOrSorceryCastThisTurnEffect(
        Integer maxManaValue,
        DynamicAmount dynamicCopyCount
) implements CardEffect {

    public CopyNextInstantOrSorceryCastThisTurnEffect() {
        this(null, null);
    }

    public CopyNextInstantOrSorceryCastThisTurnEffect(Integer maxManaValue) {
        this(maxManaValue, null);
    }

    /** Registers a delayed copy whose number of copies is evaluated when the next spell is cast. */
    public CopyNextInstantOrSorceryCastThisTurnEffect(DynamicAmount dynamicCopyCount) {
        this(null, dynamicCopyCount);
    }
}
