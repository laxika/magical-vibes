package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardToExileWithPlayPermissionEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchTargetLibraryForCardToExileWithPlayPermissionEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetLibraryForCardToExileWithPlayPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean expiresAtNextUpkeep =
                effect instanceof SearchTargetLibraryForCardToExileWithPlayPermissionEffect e && e.expiresAtNextUpkeep();
        doResolve(gameData, entry, expiresAtNextUpkeep);
    }

    private void doResolve(GameData gameData, StackEntry entry, boolean expiresAtNextUpkeep) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = controllerName + " searches " + targetName + "'s library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> allCards = new ArrayList<>(deck);

        LibrarySearchDestination destination = expiresAtNextUpkeep
                ? LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP
                : LibrarySearchDestination.EXILE_PLAYABLE;

        String prompt = "Search " + targetName + "'s library for a card to exile face down.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, allCards)
                .targetPlayerId(targetPlayerId)
                .destination(destination)
                .sourceCards(List.of(entry.getCard()))
                .build(), prompt, false, controllerName + " searches " + targetName + "'s library.");

        log.info("Game {} - {} searching {}'s library for Praetor's Grasp ({} cards in library)",
                gameData.id, controllerName, targetName, allCards.size());
    }
}
