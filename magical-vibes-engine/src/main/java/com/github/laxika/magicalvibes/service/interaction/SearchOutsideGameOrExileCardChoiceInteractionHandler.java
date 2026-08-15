package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchOutsideGameOrExileCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SearchOutsideGameOrExileCardChoice> {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.SearchOutsideGameOrExileCardChoice> handledType() {
        return PendingInteraction.SearchOutsideGameOrExileCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SearchOutsideGameOrExileCardChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() > 1
                || (chosenIds.size() == 1 && !interaction.validCardIds().contains(chosenIds.getFirst()))) {
            throw new IllegalStateException("Choose at most one " + interaction.cardLabel());
        }

        UUID playerId = interaction.playerId();
        Card chosenCard = chosenIds.isEmpty() ? null : findEligibleCard(gameData, playerId,
                chosenIds.getFirst(), interaction.filter());
        if (!chosenIds.isEmpty() && chosenCard == null) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        if (chosenCard == null) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to put a "
                            + interaction.cardLabel() + " into their hand."));
        } else {
            List<Card> sideboard = gameData.playerSideboards.get(playerId);
            boolean fromSideboard = sideboard != null && sideboard.removeIf(
                    card -> card.getId().equals(chosenCard.getId()));
            if (!fromSideboard && !gameData.removeFromExile(chosenCard.getId())) {
                throw new IllegalStateException("Chosen card is no longer available");
            }
            gameData.addCardToHand(playerId, chosenCard);
            if (fromSideboard) {
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(playerId) + " reveals ", chosenCard,
                        " and puts it into their hand."));
            } else {
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(playerId) + " puts ", chosenCard,
                        " from exile into their hand."));
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private Card findEligibleCard(GameData gameData, UUID playerId, UUID cardId,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate filter) {
        List<Card> sideboard = gameData.playerSideboards.getOrDefault(playerId, List.of());
        for (Card card : sideboard) {
            if (card.getId().equals(cardId)
                    && predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, playerId)) {
                return card;
            }
        }

        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (playerId.equals(exiled.ownerId()) && !exiled.faceDown()
                        && exiled.card().getId().equals(cardId)
                        && predicateEvaluationService.matchesCardPredicate(
                                exiled.card(), filter, null, gameData, playerId)) {
                    return exiled.card();
                }
            }
        }
        return null;
    }
}
