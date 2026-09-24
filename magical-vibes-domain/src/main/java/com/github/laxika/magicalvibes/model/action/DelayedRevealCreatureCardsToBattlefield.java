package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Reveals a snapshotted number of creature cards from a library at the next end step. */
public record DelayedRevealCreatureCardsToBattlefield(
        UUID controllerId,
        Card sourceCard,
        int creatureCount
) implements DelayedAction {
}
