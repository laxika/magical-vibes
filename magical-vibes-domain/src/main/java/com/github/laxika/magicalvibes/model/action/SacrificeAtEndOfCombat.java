package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/**
 * Permanent scheduled for sacrifice when combat ends (e.g. an attack-triggered temporary creature).
 * When {@code damageToController} is greater than zero, the source also deals that much damage to
 * {@code controllerId} at end of combat (Time Elemental). {@code sourceCard} is captured for
 * last-known-information damage if the permanent already left the battlefield.
 * <p>
 * {@code tokenForSacrificingPlayer}, when non-null, is created for the controller of the sacrificed
 * permanent immediately after the sacrifice — the "If the player does, they create a … token" rider
 * of Basalt Golem. Nothing is created when the permanent already left the battlefield, since then no
 * sacrifice happened.
 */
public record SacrificeAtEndOfCombat(UUID permanentId, UUID controllerId, Card sourceCard,
                                     int damageToController,
                                     CreateTokenEffect tokenForSacrificingPlayer) implements DelayedAction {

    public SacrificeAtEndOfCombat(UUID permanentId) {
        this(permanentId, null, null, 0, null);
    }

    public SacrificeAtEndOfCombat(UUID permanentId, UUID controllerId, Card sourceCard, int damageToController) {
        this(permanentId, controllerId, sourceCard, damageToController, null);
    }
}
