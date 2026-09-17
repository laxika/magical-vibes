package com.github.laxika.magicalvibes.service;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
@Service
public class CommanderZoneMoveService {
    @Autowired @Lazy private InteractionHandlerRegistry interactions;
    public boolean beginPending(GameData game) {
        if (game.pendingCommanderZoneMoves.isEmpty()) return false;
        if (!game.interaction.isAwaitingInput()) interactions.begin(game,
                new PendingInteraction.CommanderReplacementChoice(game.pendingCommanderZoneMoves.getFirst()));
        return true;
    }
}
