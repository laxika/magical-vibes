package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Grants the source permanent a temporary triggered ability whose event subject must match the
 * color chosen earlier during the same resolution.
 */
public record GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {
}
