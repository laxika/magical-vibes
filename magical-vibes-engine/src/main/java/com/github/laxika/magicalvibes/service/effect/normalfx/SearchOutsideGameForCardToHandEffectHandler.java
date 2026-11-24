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

/** Offers a matching card from the controller's outside-the-game pool, into their hand or on top of their library. */
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
        List<Card> matchingCards = com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, controllerId).stream()
                .filter(card -> search.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, search.filter(), null, gameData, controllerId))
                .toList();

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " finds no "
                            + CardPredicateUtils.describeFilter(search.filter())
                            + " outside the game."));
            return;
        }

        boolean toHand = search.destination() == LibrarySearchDestination.HAND;
        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .reveals(search.reveals())
                .canFailToFind(true)
                .remainingCount(1)
                .destination(search.destination())
                .filterPredicate(search.filter())
                .shuffleAfterSelection(false)
                .sourceSideboard(true)
                .build();
        String what = search.filter() == null ? "a card" : "a matching card";
        String prompt = search.reveals()
                ? "You may reveal " + what + " you own from outside the game and put it into your hand."
                : "You may put " + what + " you own from outside the game "
                + (toHand ? "into your hand." : "on top of your library.");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
    }
}
