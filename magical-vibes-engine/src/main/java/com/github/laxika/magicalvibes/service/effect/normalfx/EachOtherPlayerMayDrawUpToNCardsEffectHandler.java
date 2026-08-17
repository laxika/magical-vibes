package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayDrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves an independent up-to-N draw choice for each player other than the resolving controller.
 */
@Component
@RequiredArgsConstructor
public class EachOtherPlayerMayDrawUpToNCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOtherPlayerMayDrawUpToNCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOtherPlayerMayDrawUpToNCardsEffect) effect;

        if (gameData.chosenXValue != null) {
            int chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = gameData.pendingEachOtherPlayerDrawUpToQueue.remove(0);
            applyDraw(gameData, entry, playerId, chosen);
            promptNextOrFinish(gameData, entry, e);
            return;
        }

        gameData.pendingEachOtherPlayerDrawUpToQueue.clear();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(entry.getControllerId())) {
            gameData.pendingEachOtherPlayerDrawUpToQueue.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(entry.getControllerId())) {
                gameData.pendingEachOtherPlayerDrawUpToQueue.add(playerId);
            }
        }
        promptNextOrFinish(gameData, entry, e);
    }

    private void promptNextOrFinish(GameData gameData, StackEntry entry,
                                    EachOtherPlayerMayDrawUpToNCardsEffect effect) {
        if (gameData.pendingEachOtherPlayerDrawUpToQueue.isEmpty()) {
            return;
        }
        UUID playerId = gameData.pendingEachOtherPlayerDrawUpToQueue.getFirst();
        String cardName = entry.getCard().getName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, effect.max(), "Draw up to " + effect.max() + " cards for " + cardName + ".", cardName));
    }

    private void applyDraw(GameData gameData, StackEntry entry, UUID playerId, int chosen) {
        if (chosen <= 0) {
            return;
        }
        playerInteractionSupport.applyDrawCards(gameData, playerId, chosen);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + " draws " + chosen + " card" + (chosen != 1 ? "s" : "") + "."));
    }
}
