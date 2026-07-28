package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * "You choose which creatures block this combat and how those creatures block." Registered by
 * Melee. While present, the declare-blockers interaction is handed to {@code chooserId} instead of
 * the defending player; the blocking creatures are still the defending player's. Cleared when
 * combat state is cleared (end of combat).
 */
public record DelayedBlockerDeclarationControl(UUID chooserId, Card sourceCard)
        implements DelayedAction {
}
