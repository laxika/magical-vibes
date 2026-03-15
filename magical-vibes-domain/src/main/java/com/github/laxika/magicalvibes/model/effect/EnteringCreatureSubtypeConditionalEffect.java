package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Conditional wrapper for ally-creature-enters triggers: the wrapped effect only fires
 * if the entering creature has the specified {@code subtype}.
 * <p>
 * Used by cards like Champion of the Parish ("whenever another Human you control enters,
 * put a +1/+1 counter on Champion of the Parish") and similar subtype-specific triggers.
 */
public record EnteringCreatureSubtypeConditionalEffect(
        CardSubtype subtype,
        CardEffect wrapped
) implements CardEffect {
}
