package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles single-pick permanent / any-target choices. The message re-sends the exact
 * begin-time ordered ID lists and prompt (the two begin variants — plain permanent vs
 * any-target with a separate player-ID list — differ only in the lists they carry).
 * The per-variant "Awaiting ..." log lines stay at the {@code PlayerInputService} begin
 * helpers, so reconnect replay does not re-log. The answer (the ~45-branch
 * {@code PermanentChoiceContext} dispatch plus the pending-aura fallback) is applied by
 * {@link PermanentChoiceHandlerService#handlePermanentChosen}.
 */
@Component
@RequiredArgsConstructor
public class PermanentChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PermanentChoice> {

    private final SessionManager sessionManager;
    private final PermanentChoiceHandlerService permanentChoiceHandlerService;

    @Override
    public Class<PendingInteraction.PermanentChoice> handledType() {
        return PendingInteraction.PermanentChoice.class;
    }

    @Override
    public AwaitingInput legacyInputType() {
        return AwaitingInput.PERMANENT_CHOICE;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.PermanentChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.PermanentChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.PermanentChoice interaction, UUID recipientId) {
        sessionManager.sendToPlayer(recipientId, new ChoosePermanentMessage(
                interaction.validPermanentIds(), interaction.validPlayerIds(), interaction.prompt()));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.PermanentChoice interaction,
                             InteractionAnswer answer) {
        UUID permanentId = ((InteractionAnswer.PermanentChosen) answer).permanentId();
        permanentChoiceHandlerService.handlePermanentChosen(gameData, player, permanentId);
    }
}
