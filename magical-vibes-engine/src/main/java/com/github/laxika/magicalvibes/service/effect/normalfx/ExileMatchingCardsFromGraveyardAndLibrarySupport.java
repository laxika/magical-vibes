package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.ExileMatchingCardsFromGraveyardAndLibraryContext;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileMatchingCardsFromGraveyardAndLibrarySupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PlayerInputService playerInputService;
    private final LibrarySearchSupport librarySearchSupport;
    private final GameLogService gameLogService;

    public void begin(GameData gameData, UUID controllerId, CardPredicate filter) {
        List<Card> matchingGraveyardCards = gameData.playerGraveyards
                .getOrDefault(controllerId, List.of()).stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, controllerId))
                .toList();

        if (!matchingGraveyardCards.isEmpty()) {
            gameData.graveyardTargetOperation.resolutionTimeExileMatchingCardsResume =
                    new ExileMatchingCardsFromGraveyardAndLibraryContext(controllerId, filter);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId,
                    matchingGraveyardCards, matchingGraveyardCards.size(),
                    "Choose any number of matching cards from your graveyard to exile.");
            return;
        }

        beginLibrarySearch(gameData, controllerId, filter);
    }

    public boolean completeGraveyardChoice(GameData gameData, UUID controllerId, CardPredicate filter,
                                           List<UUID> cardIds) {
        for (UUID cardId : cardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card == null) {
                continue;
            }
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
            exileService.exileCard(gameData, controllerId, card);
            grantCastPermission(gameData, controllerId, card);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " exiles ", card, " from their graveyard."));
        }

        return beginLibrarySearch(gameData, controllerId, filter);
    }

    private boolean beginLibrarySearch(GameData gameData, UUID controllerId, CardPredicate filter) {
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return false;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " searches their library but it is empty. Library is shuffled."));
            return false;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " searches their library but finds no matching cards. Library is shuffled."));
            return false;
        }

        String prompt = "Search your library for matching cards to exile (any number).";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(matchingCards.size())
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.EXILE_PLAYABLE_ANY_NUMBER)
                        .filterPredicate(filter)
                        .build(),
                prompt, true);
        return true;
    }

    public static void grantCastPermission(GameData gameData, UUID controllerId, Card card) {
        gameData.exilePlayPermissions.put(card.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
    }
}
