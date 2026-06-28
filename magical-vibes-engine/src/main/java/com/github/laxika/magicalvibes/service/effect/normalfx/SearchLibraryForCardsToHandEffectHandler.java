package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForCardsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCardsToHandEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                            SearchLibraryForCardsToHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        int count = entry.isCastWithFlashback() ? effect.castFromGraveyardCount() : effect.count();
        CardPredicate filter = effect.filter();
        boolean restricted = filter != null;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> matchingCards = restricted
                ? deck.stream().filter(card -> gameQueryService.matchesCardPredicate(card, filter, null)).toList()
                : new ArrayList<>(deck);

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String desc = CardPredicateUtils.describeFilter(filter).replace(" card", " cards");
            String logMsg = playerName + " searches their library but finds no " + desc + ". Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String desc = CardPredicateUtils.describeFilter(filter);
        String verb = restricted ? " to reveal and put into your hand" : " to put into your hand";
        String remainingText = count > 1 ? " (" + count + " remaining)" : "";
        String prompt = "Search your library for a " + desc + verb + remainingText + ".";

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .remainingCount(count)
                .reveals(restricted)
                .canFailToFind(restricted)
                .destination(LibrarySearchDestination.HAND)
                .filterPredicate(restricted ? filter : null)
                .build(), prompt, restricted);

        log.info("Game {} - {} searches library for {} card(s) to hand ({} matches)",
                gameData.id, playerName, count, matchingCards.size());
    }
}
