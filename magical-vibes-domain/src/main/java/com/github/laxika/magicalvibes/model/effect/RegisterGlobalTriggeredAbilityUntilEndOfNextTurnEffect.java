package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/** Registers a global triggered ability through the end of the controller's next turn. */
public record RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(EffectSlot slot,
                                                                      CardEffect triggeredEffect,
                                                                      TargetFilter targetFilter)
        implements CardEffect {
}
