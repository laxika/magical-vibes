package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SphinxAmbassadorEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SphinxAmbassadorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry);
    }

    private void doResolve(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = controllerName + " searches " + targetName + "'s library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> allCards = new ArrayList<>(deck);

        // Store the source card for the may ability description later
        gameData.queueInteraction(new com.github.laxika.magicalvibes.model.PendingSphinxAmbassadorChoice(
                null, controllerId, targetPlayerId, entry.getCard()));

        String prompt = "Search " + targetName + "'s library for a card.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, allCards)
                .targetPlayerId(targetPlayerId)
                .destination(LibrarySearchDestination.SPHINX_AMBASSADOR)
                .shuffleAfterSelection(false)
                .build(), prompt, false, controllerName + " searches " + targetName + "'s library.");

        log.info("Game {} - {} searching {}'s library for Sphinx Ambassador ({} cards in library)",
                gameData.id, controllerName, targetName, allCards.size());
    }
}
