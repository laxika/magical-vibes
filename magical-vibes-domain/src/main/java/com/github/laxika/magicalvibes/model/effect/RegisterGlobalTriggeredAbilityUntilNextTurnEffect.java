package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/** Registers a global triggered ability through the beginning of the controller's next turn. */
public record RegisterGlobalTriggeredAbilityUntilNextTurnEffect(EffectSlot slot,
                                                                 CardEffect triggeredEffect)
        implements CardEffect {
}
