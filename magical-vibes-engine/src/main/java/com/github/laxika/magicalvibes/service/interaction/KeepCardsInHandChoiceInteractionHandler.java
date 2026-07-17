package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.KeepCardsInHandSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles Worldpurge's per-player keep choice: the deciding player picks up to seven cards in
 * their hand to keep, and the rest are shuffled into their library. Answering advances to the next
 * remaining player (APNAP order) with a non-empty hand; once all players have chosen, resolution of
 * the spell resumes (e.g. the "each player loses all unspent mana" step).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeepCardsInHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.KeepCardsInHandChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final KeepCardsInHandSupport keepCardsInHandSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.KeepCardsInHandChoice> handledType() {
        return PendingInteraction.KeepCardsInHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.KeepCardsInHandChoice interaction, UUID recipientId) {
        List<Card> hand = gameData.playerHands.get(interaction.playerId());
        List<CardView> cardViews = hand == null ? List.of()
                : hand.stream()
                        .filter(c -> interaction.validCardIds().contains(c.getId()))
                        .map(cardViewFactory::create)
                        .toList();

        sessionManager.sendToPlayer(recipientId,
                new ChooseMultipleCardsMessage(new ArrayList<>(interaction.validCardIds()), cardViews,
                        interaction.maxCount(),
                        "Choose up to seven cards in your hand to keep. Shuffle the rest into your library."));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.KeepCardsInHandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> keptCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (keptCardIds == null) {
            keptCardIds = List.of();
        }
        if (keptCardIds.size() > interaction.maxCount()) {
            throw new IllegalStateException("Too many cards kept (max " + interaction.maxCount() + ")");
        }
        for (UUID id : keptCardIds) {
            if (!interaction.validCardIds().contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }

        gameData.interaction.clearAwaitingInput();
        keepCardsInHandSupport.applyKeepChoice(gameData, player.getId(), keptCardIds, interaction.cardName());
        gameBroadcastService.broadcastGameState(gameData);

        boolean begunNext = keepCardsInHandSupport.beginNextChoice(gameData,
                interaction.remainingPlayerIds(), interaction.keepCount(), interaction.cardName());
        if (!begunNext) {
            // All players have chosen — resume the rest of the spell (e.g. the mana-loss step).
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
