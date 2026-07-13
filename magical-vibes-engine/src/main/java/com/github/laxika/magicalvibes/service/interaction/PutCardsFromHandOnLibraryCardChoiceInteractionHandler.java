package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Prompts the player to choose the hand cards they will put on top/bottom of their library
 * (Dream Cache's "put two cards from your hand"). On answer, begins the follow-up
 * {@link PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice} for the top/bottom pick.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCardsFromHandOnLibraryCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutCardsFromHandOnLibraryCardChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.PutCardsFromHandOnLibraryCardChoice> handledType() {
        return PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.PutCardsFromHandOnLibraryCardChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice interaction,
                       UUID recipientId) {
        List<CardView> cardViews = interaction.cards().stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(recipientId, new ChooseMultipleCardsMessage(
                new ArrayList<>(interaction.validCardIds()), cardViews, interaction.maxCount(),
                "Choose " + interaction.maxCount() + " card(s) to put on top or bottom of your library."));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutCardsFromHandOnLibraryCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();

        // Keep only valid, unique picks, capped at the allowed count.
        List<UUID> validated = new ArrayList<>();
        for (UUID id : chosenIds) {
            if (interaction.validCardIds().contains(id) && !validated.contains(id)) {
                validated.add(id);
            }
            if (validated.size() >= interaction.maxCount()) {
                break;
            }
        }

        gameData.interaction.clearAwaitingInput();

        if (validated.isEmpty()) {
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice(player.getId(), validated));
    }
}
