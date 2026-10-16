package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchHandAndOrLibraryForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchHandAndOrLibraryForCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchHandAndOrLibraryForCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchHandAndOrLibraryForCardToBattlefieldEffect search =
                (SearchHandAndOrLibraryForCardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        boolean librarySearchAllowed = !librarySearchSupport.isSearchPrevented(gameData, controllerId, false);
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        // Only a permanent card can be put onto the battlefield, so any other card is never offered.
        CardPredicate battlefieldFilter = new CardAllOfPredicate(List.of(search.filter(), new CardIsPermanentPredicate()));

        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        List<Card> handMatches = hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, battlefieldFilter, sourceCardId, gameData, controllerId, null, null, entry.getXValue()))
                .toList();

        List<Card> libraryMatches = List.of();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (librarySearchAllowed && deck != null) {
            int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
            libraryMatches = deck.stream()
                    .limit(Math.min(topLimit, deck.size()))
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, battlefieldFilter, sourceCardId, gameData, controllerId, null, null, entry.getXValue()))
                    .toList();
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String description = CardPredicateUtils.describeFilter(search.filter());
        if (handMatches.isEmpty() && libraryMatches.isEmpty()) {
            // The controller chooses whether to search; with nothing to find, the library is not searched.
            gameLogService.append(gameData, GameLog.text(playerName
                    + " has no " + description + " to put onto the battlefield."));
            return;
        }

        List<Card> pool = new ArrayList<>(handMatches);
        pool.addAll(libraryMatches);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SearchHandAndOrLibraryChoice(
                controllerId, pool,
                new HashSet<>(libraryMatches.stream().map(Card::getId).toList()),
                new HashSet<>(handMatches.stream().map(Card::getId).toList()),
                librarySearchAllowed, description, LibrarySearchDestination.BATTLEFIELD));
        gameLogService.append(gameData, GameLog.text(playerName + " searches their hand and library."));
        log.info("Game {} - {} searches hand and library for a card to battlefield", gameData.id, playerName);
    }
}
