package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Registers a global triggered ability until end of turn. This is used for effects that watch an
 * event across all players' permanents and therefore cannot be stored on one permanent.
 */
public record RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(EffectSlot slot,
                                                                 CardEffect triggeredEffect)
        implements CardEffect {
}
