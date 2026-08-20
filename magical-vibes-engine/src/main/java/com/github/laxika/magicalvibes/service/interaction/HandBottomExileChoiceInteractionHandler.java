package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles the three-way library distribution used by Expressive Iteration. */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandBottomExileChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.HandBottomExileChoice> {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.HandBottomExileChoice> handledType() {
        return PendingInteraction.HandBottomExileChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.HandBottomExile.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.HandBottomExileChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        InteractionAnswer.HandBottomExile choice = (InteractionAnswer.HandBottomExile) answer;
        int handCardIndex = choice.handCardIndex();
        int bottomCardIndex = choice.bottomCardIndex();
        List<Card> cards = interaction.cards();
        if (handCardIndex < 0 || handCardIndex >= cards.size()) {
            throw new IllegalStateException("Invalid hand card index: " + handCardIndex);
        }
        if (bottomCardIndex < 0 || bottomCardIndex >= cards.size()) {
            throw new IllegalStateException("Invalid bottom card index: " + bottomCardIndex);
        }
        if (handCardIndex == bottomCardIndex) {
            throw new IllegalStateException("Hand and bottom card indices must be different");
        }

        UUID playerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(playerId);
        Card handCard = cards.get(handCardIndex);
        Card bottomCard = cards.get(bottomCardIndex);
        gameData.addCardToHand(playerId, handCard);
        deck.add(bottomCard);

        Card exiledCard = null;
        for (int i = 0; i < cards.size(); i++) {
            if (i != handCardIndex && i != bottomCardIndex) {
                Card card = cards.get(i);
                exileService.exileCard(gameData, playerId, card);
                gameData.exilePlayPermissions.put(card.getId(), playerId);
                gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
                if (exiledCard == null) {
                    exiledCard = card;
                }
            }
        }

        gameData.interaction.clearAwaitingInput();
        String logMessage = player.getUsername()
                + " puts one card into their hand, one on the bottom of their library, and exiles "
                + (exiledCard == null ? "no cards" : "one card") + ".";
        gameLogService.append(gameData, GameLog.text(logMessage));
        log.info("Game {} - {} completed hand/bottom/exile choice", gameData.id, player.getUsername());
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }
}
