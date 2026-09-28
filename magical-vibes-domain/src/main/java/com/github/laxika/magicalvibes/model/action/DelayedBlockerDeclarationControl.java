package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * "You choose which creatures block this combat and how those creatures block." Registered by
 * Melee and Master Warcraft. While present, the declare-blockers interaction is handed to
 * {@code chooserId}; the blocking creatures are still the defending player's. Combat-scoped
 * controls expire after combat, while Master Warcraft persists through the turn.
 */
public record DelayedBlockerDeclarationControl(UUID chooserId, Card sourceCard,
                                                boolean untilEndOfTurn) implements DelayedAction {

    public DelayedBlockerDeclarationControl(UUID chooserId, Card sourceCard) {
        this(chooserId, sourceCard, false);
    }
}
