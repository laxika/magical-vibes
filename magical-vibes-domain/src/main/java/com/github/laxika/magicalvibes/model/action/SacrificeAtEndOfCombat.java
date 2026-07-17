package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Permanent scheduled for sacrifice when combat ends (e.g. an attack-triggered temporary creature).
 * When {@code damageToController} is greater than zero, the source also deals that much damage to
 * {@code controllerId} at end of combat (Time Elemental). {@code sourceCard} is captured for
 * last-known-information damage if the permanent already left the battlefield.
 */
public record SacrificeAtEndOfCombat(UUID permanentId, UUID controllerId, Card sourceCard,
                                     int damageToController) implements DelayedAction {

    public SacrificeAtEndOfCombat(UUID permanentId) {
        this(permanentId, null, null, 0);
    }
}
