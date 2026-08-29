package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a search for a matching card in the controller's sideboard. */
@Component
@RequiredArgsConstructor
public class SearchOutsideGameForCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchOutsideGameForCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var search = (SearchOutsideGameForCardToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> matchingCards = gameData.playerSideboards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> search.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, search.filter(), null, gameData, controllerId))
                .toList();

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " finds no "
                            + CardPredicateUtils.describeFilter(search.filter())
                            + " card outside the game."));
            return;
        }

        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .reveals(true)
                .canFailToFind(true)
                .remainingCount(1)
                .destination(LibrarySearchDestination.HAND)
                .filterPredicate(search.filter())
                .shuffleAfterSelection(false)
                .sourceSideboard(true)
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                params,
                "You may reveal a matching card you own from outside the game and put it into your hand.",
                true));
    }
}
