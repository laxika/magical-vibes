package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles "exile any number of cards named X" choices spanning the target player's hand,
 * graveyard, and library (e.g. Memoricide-style effects). Card views are re-derived by the
 * same hand → graveyard → library scan the begin sites use; the answer (the actual exiling
 * and library shuffle) is applied by {@link ChoiceHandlerService#handleMultiZoneExileCardsChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiZoneExileChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MultiZoneExileChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final ChoiceHandlerService choiceHandlerService;

    @Override
    public Class<PendingInteraction.MultiZoneExileChoice> handledType() {
        return PendingInteraction.MultiZoneExileChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.MultiZoneExileChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.MultiZoneExileChoice interaction, UUID recipientId) {
        List<CardView> cardViews = new ArrayList<>();
        UUID targetPid = interaction.targetPlayerId();
        for (Card card : gameData.playerHands.getOrDefault(targetPid, List.of())) {
            if (interaction.validCardIds().contains(card.getId())) {
                cardViews.add(cardViewFactory.create(card));
            }
        }
        for (Card card : gameData.playerGraveyards.getOrDefault(targetPid, List.of())) {
            if (interaction.validCardIds().contains(card.getId())) {
                cardViews.add(cardViewFactory.create(card));
            }
        }
        for (Card card : gameData.playerDecks.getOrDefault(targetPid, List.of())) {
            if (interaction.validCardIds().contains(card.getId())) {
                cardViews.add(cardViewFactory.create(card));
            }
        }

        sessionManager.sendToPlayer(recipientId, new ChooseMultipleCardsMessage(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                "Choose any number of cards named \"" + interaction.cardName() + "\" to exile."));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose cards to exile (up to {})",
                gameData.id, playerName, interaction.maxCount());
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MultiZoneExileChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        choiceHandlerService.handleMultiZoneExileCardsChosen(gameData, player, cardIds);
    }
}
