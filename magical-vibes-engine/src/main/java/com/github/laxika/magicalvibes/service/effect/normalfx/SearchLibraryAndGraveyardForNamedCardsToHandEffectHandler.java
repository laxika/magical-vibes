package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndGraveyardForNamedCardsToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryAndGraveyardForNamedCardsToHandEffect}: auto-takes each listed name
 * from the graveyard when present, then queues the remainder as named library-to-hand picks via
 * {@link LibrarySearchSupport#startNextToHandPick} (single shuffle when the queue empties). If every
 * name was found in the graveyard, the library is still shuffled once — the instruction searches
 * both zones.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryAndGraveyardForNamedCardsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndGraveyardForNamedCardsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryAndGraveyardForNamedCardsToHandEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                           SearchLibraryAndGraveyardForNamedCardsToHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<String> remainingForLibrary = new ArrayList<>();

        for (String cardName : effect.cardNames()) {
            Optional<Card> graveyardMatch = graveyard == null ? Optional.empty()
                    : graveyard.stream().filter(card -> cardName.equals(card.getName())).findFirst();
            if (graveyardMatch.isPresent()) {
                Card found = graveyardMatch.get();
                graveyard.remove(found);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, found);
                gameData.playerHands.get(controllerId).add(found);
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " searches their graveyard, reveals ", found,
                        ", and puts it into their hand."));
                log.info("Game {} - {} finds {} in graveyard", gameData.id, playerName, cardName);
            } else {
                remainingForLibrary.add(cardName);
            }
        }

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        if (remainingForLibrary.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is shuffled."));
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        librarySearchSupport.startNextToHandPick(gameData, controllerId,
                LibrarySearchFollowUp.namedToHandPicks(remainingForLibrary));
    }
}
