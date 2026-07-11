package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/** Delayed trigger: "This turn, whenever an attacking creature deals combat damage to you, it deals
 *  that much damage to its controller." Registered by Harsh Justice. {@code protectedPlayerId} is the
 *  player the reflection defends (the spell's controller). Cleared at start of new turn. */
public record DelayedCombatDamageReflection(UUID protectedPlayerId, Card sourceCard) implements DelayedAction {
}
