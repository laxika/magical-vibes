package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Applies the hand card choice for an exchange effect. */
@Component
@RequiredArgsConstructor
public class ExchangeOutsideGameHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExchangeOutsideGameHandChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ExchangeOutsideGameHandChoice> handledType() {
        return PendingInteraction.ExchangeOutsideGameHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExchangeOutsideGameHandChoice interaction,
                             InteractionAnswer answer) {
        UUID playerId = interaction.playerId();
        if (!playerId.equals(player.getId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        if (!interaction.validIndices().contains(cardIndex) || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalStateException("Choose a valid card from your hand");
        }

        // The outside card does not change zones until both sides of the exchange are chosen.
        List<Card> outside = com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, playerId);
        if (!outside.removeIf(card -> card.getId().equals(interaction.outsideCard().getId()))) {
            throw new IllegalStateException("Chosen card is no longer available outside the game");
        }
        gameData.outsideGamePlayPermissions.remove(interaction.outsideCard().getId());
        Card handCard = hand.remove(cardIndex);
        gameData.playerSideboards.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(handCard);
        gameData.outsideGamePlayPermissions.remove(handCard.getId());
        gameData.addCardToHand(playerId, interaction.outsideCard());
        gameData.interaction.clearAwaitingInput();

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(playerId) + " exchanges ")
                .card(interaction.outsideCard())
                .text(" for ")
                .card(handCard)
                .text(".")
                .build());
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
