package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Applies the outside-game card choice for an exchange effect. */
@Component
@RequiredArgsConstructor
public class ExchangeOutsideGameCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExchangeOutsideGameCardChoice> {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ExchangeOutsideGameCardChoice> handledType() {
        return PendingInteraction.ExchangeOutsideGameCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExchangeOutsideGameCardChoice interaction,
                             InteractionAnswer answer) {
        requireDecidingPlayer(player, interaction.playerId());
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if (chosenIds.size() > 1
                || (chosenIds.size() == 1 && !interaction.validCardIds().contains(chosenIds.getFirst()))) {
            throw new IllegalStateException("Choose at most one card from outside the game");
        }

        if (chosenIds.isEmpty()) {
            gameData.interaction.clearAwaitingInput();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID playerId = interaction.playerId();
        Card chosenCard = findEligibleCard(gameData, playerId, chosenIds.getFirst(), interaction);
        if (chosenCard == null) {
            throw new IllegalStateException("Chosen card is no longer available outside the game");
        }

        List<Card> sideboard = gameData.playerSideboards.get(playerId);
        sideboard.removeIf(card -> card.getId().equals(chosenCard.getId()));
        gameData.outsideGamePlayPermissions.remove(chosenCard.getId());

        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " reveals ", chosenCard,
                " from outside the game."));

        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        if (hand.isEmpty()) {
            gameData.playerSideboards.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(chosenCard);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExchangeOutsideGameHandChoice(
                playerId,
                IntStream.range(0, hand.size()).boxed().toList(),
                "Choose a card in your hand to exchange with the revealed card.",
                chosenCard));
    }

    private Card findEligibleCard(GameData gameData, UUID playerId, UUID cardId,
                                  PendingInteraction.ExchangeOutsideGameCardChoice interaction) {
        for (Card card : gameData.playerSideboards.getOrDefault(playerId, List.of())) {
            if (card.getId().equals(cardId)
                    && (interaction.filter() == null
                    || predicateEvaluationService.matchesCardPredicate(
                    card, interaction.filter(), null, gameData, playerId))) {
                return card;
            }
        }
        return null;
    }

    private void requireDecidingPlayer(Player player, UUID playerId) {
        if (!playerId.equals(player.getId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
    }
}
