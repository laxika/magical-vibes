package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One active "whenever a card is put into an opponent's graveyard from anywhere this turn, that
 * player loses 1 life" delayed triggered ability (Duskmantle Guildmage's first ability).
 *
 * <p>Registered when the activated ability resolves and cleared at turn cleanup. Each activation
 * adds its own watcher, so activating twice makes each card put into an opponent's graveyard cause
 * two separate triggers. The watcher outlives its source permanent — once the ability has resolved,
 * the delayed trigger keeps firing for the rest of the turn even if the source leaves the
 * battlefield.
 *
 * @param controllerId the player who activated the ability; "opponent" is relative to them
 * @param sourceCard   the source card, kept for the trigger's stack entry and log text
 */
public record OpponentGraveyardLifeLossWatcher(UUID controllerId, Card sourceCard) {
}
