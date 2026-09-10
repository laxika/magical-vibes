package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Grants the source permanent a triggered ability until its controller's next upkeep.
 *
 * <p>The grant is represented as a floating continuous effect so it survives turn cleanup and
 * expires at the correct upkeep boundary.
 */
public record GrantEffectToSourceUntilNextUpkeepEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {
}
