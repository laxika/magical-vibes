package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * "You choose which creatures attack this turn." While present, the declare-attackers
 * interaction is handed to {@code chooserId} instead of the active player.
 */
public record DelayedAttackerDeclarationControl(UUID chooserId, Card sourceCard)
        implements DelayedAction {
}
