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

import java.util.List;
import java.util.UUID;

/**
 * Phase 1 of Thieving Sprite: the target picks which cards from their own hand to reveal. The target
 * sees their whole hand with the still-selectable indices highlighted; each pick is answered as an
 * {@link InteractionAnswer.CardIndexChosen} and applied by
 * {@link CardChoiceHandlerService#handleRevealCardsFromHandChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCardsFromHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.RevealCardsFromHandChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.RevealCardsFromHandChoice> handledType() {
        return PendingInteraction.RevealCardsFromHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.RevealCardsFromHandChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.RevealCardsFromHandChoice interaction, UUID recipientId) {
        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        List<CardView> cardViews = hand.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(recipientId, new ChooseFromRevealedHandMessage(
                cardViews, interaction.validIndices(), interaction.prompt(), false));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose a card to reveal", gameData.id, playerName);
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.RevealCardsFromHandChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        cardChoiceHandlerService.handleRevealCardsFromHandChosen(gameData, player, cardIndex);
    }
}
