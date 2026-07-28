package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: "Whenever a creature attacks and isn't blocked this combat, untap it and remove
 * it from combat." Registered by Melee. Fires once per unblocked attacker, regardless of who
 * controls it; {@code controllerId} is the trigger's controller (the spell's controller), not a
 * filter on the attacker. Cleared when combat state is cleared (end of combat).
 */
public record DelayedUnblockedAttackerUntapRemoveFromCombat(UUID controllerId, Card sourceCard)
        implements DelayedAction {
}
