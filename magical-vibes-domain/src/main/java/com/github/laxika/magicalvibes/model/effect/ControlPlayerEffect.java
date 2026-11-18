package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * One player controlling another (CR 724): a floating effect on the controlled player, made by Mindslaver ("during
 * that player's next turn", CR 724.1), Secret of Bloodbending ("during their next combat phase") or Word of Command
 * (CR 724.2). Of the effects on one player, the last one created is the one that works (CR 724.1a), and creating one
 * ends the older ones, except that an extra turn an older one promised is kept when the new one promises one too.
 *
 * @param controllerPlayerId the player who controls the affected player
 * @param extraTurnAfter     the affected player takes an extra turn after the controlled turn (Emrakul, the Promised
 *                           End)
 */
public record ControlPlayerEffect(UUID controllerPlayerId, boolean extraTurnAfter) implements CardEffect {
}
