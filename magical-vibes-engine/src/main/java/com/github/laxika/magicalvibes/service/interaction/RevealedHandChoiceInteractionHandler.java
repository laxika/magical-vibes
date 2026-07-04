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
 * Handles "choose a card from the target player's revealed hand" decisions (Duress-style
 * discard, exile-from-hand, and put-on-top-of-library flows, including multi-pick countdowns).
 * Card views are re-derived from the target's current hand at prompt time — identical at begin
 * and reconnect replay, since any hand change begins a fresh record. The answer (removal from
 * hand, countdown, and the final batch action) is applied by
 * {@link CardChoiceHandlerService#handleRevealedHandCardChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealedHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.RevealedHandChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.RevealedHandChoice> handledType() {
        return PendingInteraction.RevealedHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.RevealedHandChoice interaction) {
        return interaction.choosingPlayerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.RevealedHandChoice interaction, UUID recipientId) {
        List<Card> targetHand = gameData.playerHands.get(interaction.targetPlayerId());
        List<CardView> cardViews = targetHand.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(recipientId, new ChooseFromRevealedHandMessage(
                cardViews, interaction.validIndices(), interaction.prompt()));

        String playerName = gameData.playerIdToName.get(interaction.choosingPlayerId());
        log.info("Game {} - Awaiting {} to choose a card from revealed hand", gameData.id, playerName);
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.RevealedHandChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        cardChoiceHandlerService.handleRevealedHandCardChosen(gameData, player, cardIndex);
    }
}
