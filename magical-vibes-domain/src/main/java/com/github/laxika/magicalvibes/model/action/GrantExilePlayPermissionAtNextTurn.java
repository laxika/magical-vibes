package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Grants the owner permission to cast a Warp-exiled card starting on the following turn. */
public record GrantExilePlayPermissionAtNextTurn(UUID cardId, UUID ownerId, int exiledTurnNumber)
        implements DelayedAction {
}
