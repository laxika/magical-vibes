package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardsToExileAndCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardsToExileAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForCardsToExileAndCreateTokensEffect searchEffect =
                (SearchLibraryForCardsToExileAndCreateTokensEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        CardPredicate filter = searchEffect.filter();
        List<Card> matchingCards = deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, controllerId))
                .toList();
        String description = CardPredicateUtils.describeFilter(filter);
        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no "
                    + description + ". Library is shuffled."));
            return;
        }

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(matchingCards.size())
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.EXILE_AND_CREATE_TOKENS)
                        .filterPredicate(filter)
                        .accumulatedCards(List.of())
                        .tokenTemplate(searchEffect.tokenTemplate())
                        .sourceSetCode(entry.getCard().getSetCode())
                        .build(),
                "Search your library for a " + description + " to exile (any number).", true);

        log.info("Game {} - {} searches library to exile any number of {} and create tokens",
                gameData.id, playerName, description);
    }
}
