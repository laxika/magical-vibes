package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles single-card graveyard choices (return to hand/battlefield, exile, may-ability
 * targeting). The all-graveyards flag on the message derives from the record's card pool
 * (non-null = cross-graveyard choice). The answer (removal, destination handling, exile
 * countdown, return-queue continuation) is applied by
 * {@link GraveyardChoiceHandlerService#handleGraveyardCardChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.GraveyardChoice> {

    private final SessionManager sessionManager;
    private final GraveyardChoiceHandlerService graveyardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.GraveyardChoice> handledType() {
        return PendingInteraction.GraveyardChoice.class;
    }

    @Override
    public AwaitingInput legacyInputType() {
        return AwaitingInput.GRAVEYARD_CHOICE;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.GraveyardCardChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.GraveyardChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.GraveyardChoice interaction, UUID recipientId) {
        boolean allGraveyards = interaction.cardPool() != null;
        sessionManager.sendToPlayer(recipientId, new ChooseCardFromGraveyardMessage(
                interaction.validIndices(), interaction.prompt(), allGraveyards));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose a card from graveyard", gameData.id, playerName);
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.GraveyardChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.GraveyardCardChosen) answer).cardIndex();
        graveyardChoiceHandlerService.handleGraveyardCardChosen(gameData, player, cardIndex);
    }
}
