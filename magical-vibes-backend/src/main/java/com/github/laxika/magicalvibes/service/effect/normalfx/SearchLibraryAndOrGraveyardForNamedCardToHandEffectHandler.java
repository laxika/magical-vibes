package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryAndOrGraveyardForNamedCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForNamedCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryAndOrGraveyardForNamedCardToHandEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                               SearchLibraryAndOrGraveyardForNamedCardToHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Check graveyard first (public zone)
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            Optional<Card> graveyardMatch = graveyard.stream()
                    .filter(card -> effect.cardName().equals(card.getName()))
                    .findFirst();

            if (graveyardMatch.isPresent()) {
                Card found = graveyardMatch.get();
                graveyard.remove(found);
                gameData.playerHands.get(controllerId).add(found);
                String logMsg = playerName + " searches their graveyard, reveals " + found.getName() + ", and puts it into their hand.";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - {} finds {} in graveyard", gameData.id, playerName, effect.cardName());
                return;
            }
        }

        // Not found in graveyard — search library
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> effect.cardName().equals(card.getName()))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no cards named " + effect.cardName() + ". Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String prompt = "Search your library for a card named " + effect.cardName() + " to reveal and put into your hand.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .remainingCount(1)
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.HAND)
                .filterCardName(effect.cardName())
                .build(), prompt, true);

        log.info("Game {} - {} searches library and/or graveyard for {}", gameData.id, playerName, effect.cardName());
    }
}
