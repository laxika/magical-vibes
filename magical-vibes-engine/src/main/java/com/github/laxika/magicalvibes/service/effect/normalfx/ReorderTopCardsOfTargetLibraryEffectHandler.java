package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReorderTopCardsOfTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReorderTopCardsOfTargetLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReorderTopCardsOfTargetLibraryEffect reorder = (ReorderTopCardsOfTargetLibraryEffect) effect;

        UUID controllerId = entry.getControllerId();
        // Fall back to the controller when no explicit target is present.
        UUID targetPlayerId = entry.getTargetId() != null ? entry.getTargetId() : controllerId;
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        int count = Math.min(reorder.count(), deck.size());
        if (count == 0) {
            String logMsg = entry.getCard().getName() + ": " + targetName + "'s library is empty, nothing to reorder.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        if (count == 1) {
            String logMsg = controllerName + " looks at the top card of " + targetName + "'s library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, topCards, false, targetPlayerId,
                "Put these cards back on top of the library in any order (top to bottom)."));

        String logMsg = controllerName + " looks at the top " + count + " cards of " + targetName + "'s library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} reordering top {} cards of {}'s library", gameData.id, controllerName, count, targetName);
    }
}
