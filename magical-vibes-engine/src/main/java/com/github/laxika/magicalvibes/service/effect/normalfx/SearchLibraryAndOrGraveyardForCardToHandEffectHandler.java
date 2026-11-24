package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.OutsideGameCards;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Offers one matching card the controller owns from the effect's zones: their library (unless a rule prevents the
 * search), graveyard, cards outside the game (judged as they are: no effect of the game applies to them, CR 400.11)
 * and face-up exile (a face-down card has no characteristics to match, CR 406.3).
 */
@Component
@RequiredArgsConstructor
public class SearchLibraryAndOrGraveyardForCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryAndOrGraveyardForCardToHandEffect search =
                (SearchLibraryAndOrGraveyardForCardToHandEffect) effect;
        UUID playerId = entry.getControllerId();
        Set<Zone> zones = search.zones();
        boolean searchesLibrary = zones.contains(Zone.LIBRARY);
        boolean librarySearchAllowed = searchesLibrary
                && !librarySearchSupport.isSearchPrevented(gameData, playerId, false);
        CardPredicate filter = search.filter();

        Map<UUID, Card> pool = new LinkedHashMap<>();
        Set<UUID> libraryCardIds = new LinkedHashSet<>();
        Set<UUID> outsideGameCardIds = new LinkedHashSet<>();
        Set<UUID> exileCardIds = new LinkedHashSet<>();
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (librarySearchAllowed && deck != null) {
            int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, playerId);
            for (Card card : deck.stream().limit(Math.min(topLimit, deck.size())).toList()) {
                if (matches(gameData, card, filter, playerId)) {
                    pool.putIfAbsent(card.getId(), card);
                    libraryCardIds.add(card.getId());
                }
            }
        }
        if (zones.contains(Zone.GRAVEYARD)) {
            for (Card card : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                if (matches(gameData, card, filter, playerId)) {
                    pool.putIfAbsent(card.getId(), card);
                }
            }
        }
        if (zones.contains(Zone.OUTSIDE_GAME)) {
            for (Card card : OutsideGameCards.view(gameData, playerId)) {
                if (matches(null, card, filter, playerId) && pool.putIfAbsent(card.getId(), card) == null) {
                    outsideGameCardIds.add(card.getId());
                }
            }
        }
        if (zones.contains(Zone.EXILE)) {
            synchronized (gameData.exiledCards) {
                for (ExiledCardEntry exiled : gameData.exiledCards) {
                    if (playerId.equals(exiled.ownerId()) && !exiled.faceDown()
                            && matches(gameData, exiled.card(), filter, playerId)
                            && pool.putIfAbsent(exiled.card().getId(), exiled.card()) == null) {
                        exileCardIds.add(exiled.card().getId());
                    }
                }
            }
        }

        String playerName = gameData.playerIdToName.get(playerId);
        String cardLabel = CardPredicateUtils.describeFilter(filter);
        if (pool.isEmpty()) {
            if (!searchesLibrary) {
                gameLogService.append(gameData, GameLog.text(playerName + " finds no " + cardLabel + " "
                        + whereNotSearched(zones) + "."));
                return;
            }
            boolean shuffled = deck != null && search.shuffleAfterSearch();
            if (librarySearchAllowed) {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, playerId);
            }
            if (shuffled) {
                LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            }
            if (librarySearchAllowed) {
                gameLogService.append(gameData, GameLog.text(playerName + " searches their " + searched(zones)
                        + " but finds no " + cardLabel + "." + (shuffled ? " Library is shuffled." : "")));
            }
            return;
        }

        UUID oppositionAgentControllerId = librarySearchAllowed
                ? librarySearchSupport.oppositionAgentControllerId(gameData, playerId)
                : null;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                playerId, new ArrayList<>(pool.values()), libraryCardIds, Set.of(), outsideGameCardIds,
                librarySearchAllowed, cardLabel, LibrarySearchDestination.HAND, null, null, 0,
                1, false, searchesLibrary && search.shuffleAfterSearch(), oppositionAgentControllerId,
                exileCardIds, search.optional()));
        if (searchesLibrary) {
            gameLogService.append(gameData, GameLog.text(playerName + " searches their " + searched(zones, "and/or")
                    + "."));
        }
    }

    /** "library and graveyard", "library, graveyard, and outside the game". */
    private static String searched(Set<Zone> zones) {
        return searched(zones, "and");
    }

    private static String searched(Set<Zone> zones, String conjunction) {
        List<String> names = new ArrayList<>();
        for (Zone zone : List.of(Zone.LIBRARY, Zone.GRAVEYARD, Zone.OUTSIDE_GAME, Zone.EXILE)) {
            if (zones.contains(zone)) {
                names.add(switch (zone) {
                    case LIBRARY -> "library";
                    case GRAVEYARD -> "graveyard";
                    case OUTSIDE_GAME -> "outside the game";
                    default -> "exile";
                });
            }
        }
        if (names.size() <= 2) {
            return String.join(" " + conjunction + " ", names);
        }
        return String.join(", ", names.subList(0, names.size() - 1)) + ", " + conjunction + " " + names.getLast();
    }

    /** Where a card taken without searching a library was looked for: "outside the game or face-up in exile". */
    private static String whereNotSearched(Set<Zone> zones) {
        List<String> names = new ArrayList<>();
        if (zones.contains(Zone.GRAVEYARD)) {
            names.add("in their graveyard");
        }
        if (zones.contains(Zone.OUTSIDE_GAME)) {
            names.add("outside the game");
        }
        if (zones.contains(Zone.EXILE)) {
            names.add("face-up in exile");
        }
        return String.join(" or ", names);
    }

    private boolean matches(GameData gameData, Card card, CardPredicate filter, UUID playerId) {
        return filter == null || predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData,
                playerId);
    }
}
