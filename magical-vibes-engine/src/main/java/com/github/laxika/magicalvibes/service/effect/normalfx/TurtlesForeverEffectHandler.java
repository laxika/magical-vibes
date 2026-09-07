package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurtlesForeverEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Turtles Forever's four-card library and outside-the-game search. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurtlesForeverEffectHandler implements NormalEffectHandlerBean {

    private static final int SEARCH_COUNT = 4;

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurtlesForeverEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        boolean librarySearchAllowed = !librarySearchSupport.isSearchPrevented(
                gameData, controllerId, false);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> searchableLibrary = deck == null ? List.of() : deck;
        if (librarySearchAllowed) {
            searchableLibrary = restrictToSearchableTopCards(gameData, controllerId, searchableLibrary);
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
        } else {
            searchableLibrary = List.of();
        }

        CardPredicate filter = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardTypePredicate(CardType.CREATURE)));
        List<Card> libraryCandidates = searchableLibrary.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, controllerId))
                .toList();
        List<Card> outsideGameCandidates = gameData.playerSideboards
                .getOrDefault(controllerId, List.of()).stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, controllerId))
                .toList();
        List<Card> pool = new ArrayList<>(libraryCandidates);
        pool.addAll(outsideGameCandidates);

        if (distinctNameCount(pool) < SEARCH_COUNT) {
            if (librarySearchAllowed) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " cannot find four legendary creature cards with different names."));
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TurtlesForeverSearchChoice(
                controllerId,
                opponentId,
                pool,
                libraryCandidates.stream().map(Card::getId).toList(),
                outsideGameCandidates.stream().map(Card::getId).toList()));
        log.info("Game {} - {} searches library and outside the game for Turtles Forever",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }

    private List<Card> restrictToSearchableTopCards(GameData gameData, UUID playerId, List<Card> deck) {
        int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, playerId);
        if (topLimit == Integer.MAX_VALUE || deck.isEmpty()) {
            return deck;
        }
        Set<Card> topCards = Collections.newSetFromMap(new IdentityHashMap<>());
        topCards.addAll(deck.subList(0, Math.min(topLimit, deck.size())));
        return deck.stream().filter(topCards::contains).toList();
    }

    private int distinctNameCount(List<Card> cards) {
        return (int) cards.stream().map(Card::getName).distinct().count();
    }
}
