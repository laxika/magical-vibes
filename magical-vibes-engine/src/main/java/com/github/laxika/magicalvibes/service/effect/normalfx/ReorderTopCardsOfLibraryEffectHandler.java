package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReorderTopCardsOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReorderTopCardsOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReorderTopCardsOfLibraryEffect reorder = (ReorderTopCardsOfLibraryEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        int count = Math.min(reorder.count(), deck.size());
        if (count == 0) {
            String logMsg = entry.getCard().getName() + ": library is empty, nothing to reorder.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), ": library is empty, nothing to reorder."));
            return;
        }

        if (count == 1) {
            String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top card of their library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), ": library is empty, nothing to reorder."));
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, topCards, false, controllerId,
                "Put these cards back on top of your library in any order (top to bottom)."));

        String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} reordering top {} cards of library", gameData.id, gameData.playerIdToName.get(controllerId), count);
    
    }
}
