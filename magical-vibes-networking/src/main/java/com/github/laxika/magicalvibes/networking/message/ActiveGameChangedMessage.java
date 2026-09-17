package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.GameContext;
import com.github.laxika.magicalvibes.networking.model.MessageType;

/** Complete replacement of the displayed game, scoped to one session activation. */
public record ActiveGameChangedMessage(MessageType type, GameContext context, int depth, JoinGame game) {
    public ActiveGameChangedMessage(GameContext context, int depth, JoinGame game) {
        this(MessageType.ACTIVE_GAME_CHANGED, context, depth, game);
    }
}
