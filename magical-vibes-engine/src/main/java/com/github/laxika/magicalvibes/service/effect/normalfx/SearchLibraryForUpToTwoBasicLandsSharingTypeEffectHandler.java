package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsSharingTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Searches for up to two basic land cards that share a land type. Both cards are chosen before either
 * is put onto the battlefield, so they enter simultaneously: the second pick is narrowed by the land
 * types of the first through {@link LibrarySearchParams#requireSharedLandType()}.
 */
@Component
@RequiredArgsConstructor
public class SearchLibraryForUpToTwoBasicLandsSharingTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForUpToTwoBasicLandsSharingTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null || librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        CardPredicate basicLand = CardPredicateUtils.basicLand();

        List<Card> matchingCards = deck == null ? List.of() : deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, basicLand, null, gameData, controllerId))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY))
                .toList();

        int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
        if (deck != null && topLimit < deck.size()) {
            List<Card> searchable = deck.subList(0, topLimit);
            matchingCards = matchingCards.stream()
                    .filter(card -> searchable.stream().anyMatch(top -> top == card))
                    .toList();
        }

        if (matchingCards.isEmpty()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, controllerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName
                    + " searches their library but finds no basic land cards. Library is shuffled."));
            return;
        }

        String prompt = "Search your library for up to two basic land cards that share a land type, "
                + "put them onto the battlefield tapped, then shuffle.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(2)
                        .canFailToFind(true)
                        .filterPredicate(basicLand)
                        .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                        .requireSharedLandType(true)
                        .prompt(prompt)
                        .build(),
                prompt, true);
    }
}
