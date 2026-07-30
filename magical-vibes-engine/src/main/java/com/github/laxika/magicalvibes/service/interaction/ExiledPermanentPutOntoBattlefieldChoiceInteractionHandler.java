package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTopCardMayPutPermanentSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Primal Surge's "you may put it onto the battlefield" prompt for the permanent card just exiled
 * from the top of the controller's library. Accepting puts it onto the battlefield and repeats the
 * process (exile the next card, prompt again if it is a permanent card); declining ends the
 * resolution and leaves the card in exile.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExiledPermanentPutOntoBattlefieldChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice> {

    private final ExileTopCardMayPutPermanentSupport support;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice> handledType() {
        return PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your " + interaction.sourceName() + " choice");
        }

        boolean accepted = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        UUID controllerId = interaction.playerId();
        String sourceName = interaction.sourceName();
        String playerName = gameData.playerIdToName.get(controllerId);

        gameData.interaction.clearAwaitingInput();

        if (accepted) {
            ExiledCardEntry entry = gameData.findExiledCard(interaction.cardId());
            if (entry != null) {
                Card card = entry.card();
                gameData.removeFromExile(card.getId());
                graveyardReturnSupport.putCardOntoBattlefieldFromExile(gameData, controllerId, card);
                log.info("Game {} - {} puts {} onto the battlefield via {}",
                        gameData.id, playerName, card.getName(), sourceName);
            }
            if (support.exileTopCardAndPromptIfPermanent(gameData, controllerId, sourceName)) {
                return;
            }
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " leaves " + interaction.cardName()
                    + " in exile (" + sourceName + ")."));
        }

        // Resumes any remaining effects/triggers and auto-passes. The shared epilogue matters here:
        // the permanents put onto the battlefield can have queued ETB may-abilities.
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
