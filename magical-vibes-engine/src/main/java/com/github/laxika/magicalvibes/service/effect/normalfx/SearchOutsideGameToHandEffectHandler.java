package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a search for a card from the controller's outside-the-game card pool. */
@Component
@RequiredArgsConstructor
public class SearchOutsideGameToHandEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchOutsideGameToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchOutsideGameToHandEffect searchEffect = (SearchOutsideGameToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        CardPredicate filter = searchEffect.filter();
        List<Card> sideboard = gameData.playerSideboards.getOrDefault(controllerId, List.of());
        List<Card> matchingCards = sideboard.stream()
                .filter(card -> filter == null
                        || predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, controllerId))
                .toList();
        String description = CardPredicateUtils.describeFilter(filter);

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " finds no " + description + " outside the game."));
            return;
        }

        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .reveals(false)
                .canFailToFind(true)
                .remainingCount(1)
                .destination(LibrarySearchDestination.HAND)
                .filterPredicate(filter)
                .shuffleAfterSelection(false)
                .sourceSideboard(true)
                .build();
        String prompt = "Choose a " + description + " you own from outside the game to put into your hand.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " searches outside the game for a " + description + "."));
    }
}
