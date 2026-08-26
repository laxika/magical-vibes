package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a search that can choose a card from the library, graveyard, or sideboard. */
@Component
@RequiredArgsConstructor
public class SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffect search =
                (SearchLibraryGraveyardAndOrOutsideGameForCardToHandEffect) effect;
        UUID playerId = entry.getControllerId();
        boolean librarySearchAllowed = !librarySearchSupport.isSearchPrevented(gameData, playerId, false);
        Map<UUID, Card> candidates = new LinkedHashMap<>();
        Set<UUID> libraryCardIds = new HashSet<>();
        Set<UUID> outsideGameCardIds = new HashSet<>();

        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        for (Card card : graveyard) {
            if (matches(gameData, card, search.filter(), playerId)) {
                candidates.put(card.getId(), card);
            }
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        if (librarySearchAllowed && deck != null) {
            int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, playerId);
            for (Card card : deck.stream().limit(Math.min(topLimit, deck.size())).toList()) {
                if (matches(gameData, card, search.filter(), playerId)) {
                    candidates.putIfAbsent(card.getId(), card);
                    libraryCardIds.add(card.getId());
                }
            }
        }

        for (Card card : gameData.playerSideboards.getOrDefault(playerId, List.of())) {
            if (matches(gameData, card, search.filter(), playerId)) {
                candidates.putIfAbsent(card.getId(), card);
                outsideGameCardIds.add(card.getId());
            }
        }

        String cardLabel = CardPredicateUtils.describeFilter(search.filter());
        if (candidates.isEmpty()) {
            if (librarySearchAllowed) {
                LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
                if (deck != null) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                }
            }
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " searches their library, graveyard, and/or outside the game but finds no "
                            + cardLabel + "." + (librarySearchAllowed && deck != null ? " Library is shuffled." : "")));
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                playerId, new ArrayList<>(candidates.values()), libraryCardIds, outsideGameCardIds,
                librarySearchAllowed, cardLabel));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(playerId) + " searches their library, graveyard, and/or outside the game."));
    }

    private boolean matches(GameData gameData, Card card, com.github.laxika.magicalvibes.model.filter.CardPredicate filter,
                            UUID playerId) {
        return filter == null || predicateEvaluationService.matchesCardPredicate(
                card, filter, null, gameData, playerId);
    }
}
