package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.service.input.MultiPermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles "select zero or more permanents" choices (sacrifice picks, proliferate targets,
 * combat-damage bounce, counter placement, …). The answer is applied by
 * {@link MultiPermanentChoiceHandlerService}, which dispatches on the pending-operation
 * flags to the matching continuation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiPermanentChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MultiPermanentChoice> {

    private final SessionManager sessionManager;
    private final MultiPermanentChoiceHandlerService multiPermanentChoiceHandlerService;

    @Override
    public Class<PendingInteraction.MultiPermanentChoice> handledType() {
        return PendingInteraction.MultiPermanentChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.PermanentsChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.MultiPermanentChoice interaction, UUID recipientId) {
        sessionManager.sendToPlayer(recipientId, new ChooseMultiplePermanentsMessage(
                new ArrayList<>(interaction.validIds()), interaction.maxCount(), interaction.prompt()));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose up to {} permanents",
                gameData.id, playerName, interaction.maxCount());
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MultiPermanentChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> permanentIds = ((InteractionAnswer.PermanentsChosen) answer).permanentIds();
        multiPermanentChoiceHandlerService.handleMultiplePermanentsChosen(gameData, player, permanentIds);
    }
}
