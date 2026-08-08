package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "Mill {@code count} cards. If at least one card matching {@code filter} is milled this way,
 * [{@code thenEffect}]" (Liliana, Untouched by Death's +1). The controller does the milling.
 *
 * <p>Only cards that actually reached the graveyard count — a replacement effect that diverts a
 * milled card elsewhere means it was not "milled this way". Wrap several follow-ups in a
 * {@link SequenceEffect}; the steps resolve in order against the same stack entry, so they must be
 * synchronous (no player-input pauses).</p>
 */
public record MillControllerThenIfMilledEffect(int count, CardPredicate filter, CardEffect thenEffect)
        implements CardEffect {
}
