package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.OutsideGameCards;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes the choice of a card outside the game that stays there, handing it to the follow-up effect. */
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
        if (chosenIds == null || chosenIds.size() != 1 || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one " + interaction.cardLabel());
        }

        UUID playerId = interaction.playerId();
        Card chosenCard = findEligibleCard(gameData, playerId, chosenIds.getFirst(), interaction.filter());
        if (chosenCard == null) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        CardEffect followUp = interaction.followUpEffect();
        if (followUp != null && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.setChosenObjectCard(chosenCard);
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, List.of(followUp));
        }
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " chooses ", chosenCard, " from outside the game."));

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private Card findEligibleCard(GameData gameData, UUID playerId, UUID cardId,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate filter) {
        for (Card card : OutsideGameCards.view(gameData, playerId)) {
            if (card.getId().equals(cardId)
                    && predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, playerId)) {
                return card;
            }
        }
        return null;
    }
}
