package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Karn Liberated restart: after the pregame procedure of the restarted game completes, the
 * cards left in exile by Karn are put onto the battlefield under the restarting player's
 * control (consumed by {@code MulliganService.continueStartGame}).
 *
 * @param cards        the cards exiled with Karn that enter the battlefield
 * @param controllerId the player who restarted the game
 */
public record PendingKarnRestart(List<Card> cards, UUID controllerId) implements PendingInteraction {
}
