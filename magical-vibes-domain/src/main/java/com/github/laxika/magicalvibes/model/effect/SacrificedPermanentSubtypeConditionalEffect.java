package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Conditional wrapper for ON_ALLY_PERMANENT_SACRIFICED triggers that checks whether
 * the sacrificed permanent had the specified subtype. If the condition is met, the
 * wrapped effect fires; otherwise the trigger is skipped.
 *
 * Example: "Whenever you sacrifice a Treasure, ..." → SacrificedPermanentSubtypeConditionalEffect(TREASURE, wrapped)
 */
public record SacrificedPermanentSubtypeConditionalEffect(
        CardSubtype requiredSubtype,
        CardEffect wrapped
) implements CardEffect {}
