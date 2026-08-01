package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: "Until your next turn, whenever a creature an opponent controls attacks, it gets
 * power/toughness until end of turn." Registered by Jace, Architect of Thought's +1.
 *
 * <p>Fires once per attacking creature controlled by a player other than {@code controllerId},
 * every time attackers are declared. Unlike the other delayed families this one is <em>not</em>
 * cleared at turn cleanup: it survives the opponents' turns and is dropped when
 * {@code controllerId}'s next turn begins, alongside the "until your next turn" floating
 * continuous effects.
 */
public record DelayedOpponentAttackerBoost(UUID controllerId, int power, int toughness, Card sourceCard)
        implements DelayedAction {
}
