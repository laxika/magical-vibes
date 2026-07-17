package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the two-stage Blackmail flow ({@link PendingInteraction.RevealCardsDiscardChoice}). In
 * the reveal stage the target player is shown their full hand and picks which cards to reveal; in
 * the discard stage the controller is shown only the revealed cards and picks one for the target to
 * discard. Both stages reuse {@link ChooseFromRevealedHandMessage}; the answer is applied by
 * {@link CardChoiceHandlerService#handleRevealCardsDiscardChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCardsDiscardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.RevealCardsDiscardChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.RevealCardsDiscardChoice> handledType() {
        return PendingInteraction.RevealCardsDiscardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.RevealCardsDiscardChoice interaction, UUID recipientId) {
        List<Card> targetHand = gameData.playerHands.get(interaction.targetPlayerId());
        List<CardView> cardViews;
        if (interaction.revealStage()) {
            // The target player sees their whole hand to pick which cards to reveal.
            cardViews = targetHand.stream().map(cardViewFactory::create).toList();
        } else {
            // The controller sees only the revealed cards.
            cardViews = new ArrayList<>();
            for (UUID cardId : interaction.revealedCardIds()) {
                targetHand.stream().filter(c -> c.getId().equals(cardId)).findFirst()
                        .ifPresent(c -> cardViews.add(cardViewFactory.create(c)));
            }
        }
        sessionManager.sendToPlayer(recipientId, new ChooseFromRevealedHandMessage(
                cardViews, interaction.validIndices(), interaction.prompt(), false));

        log.info("Game {} - Awaiting {} to choose a card (reveal-and-discard)",
                gameData.id, gameData.playerIdToName.get(interaction.decidingPlayerId()));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.RevealCardsDiscardChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        cardChoiceHandlerService.handleRevealCardsDiscardChosen(gameData, player, cardIndex);
    }
}
