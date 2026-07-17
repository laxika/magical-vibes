package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles hand/top/bottom choices ("look at the top N cards: put one into your hand, one on
 * top of your library, and the rest on the bottom"): prompts with the looked-at cards and,
 * on answer, distributes them to hand / library top / library bottom.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandTopBottomChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.HandTopBottomChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.HandTopBottomChoice> handledType() {
        return PendingInteraction.HandTopBottomChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.HandTopBottom.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.HandTopBottomChoice interaction, UUID recipientId) {
        List<CardView> cardViews = interaction.cards().stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(recipientId, new ChooseHandTopBottomMessage(
                cardViews,
                "Look at the top " + interaction.cards().size()
                        + " cards of your library. Choose one to put into your hand."));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.HandTopBottomChoice interaction,
                             InteractionAnswer answer) {
        int handCardIndex = ((InteractionAnswer.HandTopBottom) answer).handCardIndex();
        int topCardIndex = ((InteractionAnswer.HandTopBottom) answer).topCardIndex();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Card> handTopBottomCards = interaction.cards();
        int count = handTopBottomCards.size();

        if (handCardIndex < 0 || handCardIndex >= count) {
            throw new IllegalStateException("Invalid hand card index: " + handCardIndex);
        }
        if (topCardIndex < 0 || topCardIndex >= count) {
            throw new IllegalStateException("Invalid top card index: " + topCardIndex);
        }
        if (handCardIndex == topCardIndex) {
            throw new IllegalStateException("Hand and top card indices must be different");
        }

        UUID playerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(playerId);

        // Put the chosen card into hand
        Card handCard = handTopBottomCards.get(handCardIndex);
        gameData.addCardToHand(playerId, handCard);

        // Put the chosen card on top of library
        Card topCard = handTopBottomCards.get(topCardIndex);
        deck.add(0, topCard);

        // Put the remaining card on the bottom of library
        for (int i = 0; i < count; i++) {
            if (i != handCardIndex && i != topCardIndex) {
                deck.add(handTopBottomCards.get(i));
            }
        }

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();

        String logMsg;
        if (count == 2) {
            logMsg = player.getUsername() + " puts one card into their hand and one on top of their library.";
        } else {
            logMsg = player.getUsername() + " puts one card into their hand, one on top of their library, and one on the bottom.";
        }
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} completed hand/top/bottom choice", gameData.id, player.getUsername());

        turnProgressionService.resolveAutoPass(gameData);
    }
}
