package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
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

    private final PermanentChoiceHandlerService permanentChoiceHandlerService;

    @Override
    public Class<PendingInteraction.PermanentChoice> handledType() {
        return PendingInteraction.PermanentChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.PermanentChosen.class;
    }

    /**
     * A champion choice (CR 702.72a: sacrifice the permanent unless you exile another matching
     * object) is optional, so it also takes a declining may-answer.
     */
    @Override
    public boolean acceptsAnswer(PendingInteraction.PermanentChoice interaction, InteractionAnswer answer) {
        return InteractionHandler.super.acceptsAnswer(interaction, answer)
                || answer instanceof InteractionAnswer.MayAbilityChosen
                && interaction.context() instanceof PermanentChoiceContext.ChampionCreature;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.PermanentChoice interaction,
                             InteractionAnswer answer) {
        if (answer instanceof InteractionAnswer.MayAbilityChosen mayAnswer) {
            permanentChoiceHandlerService.handleChampionDeclined(gameData, player, mayAnswer.accepted());
            return;
        }
        UUID permanentId = ((InteractionAnswer.PermanentChosen) answer).permanentId();
        permanentChoiceHandlerService.handlePermanentChosen(gameData, player, permanentId);
    }
}
