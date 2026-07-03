package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles the legacy COLOR_CHOICE family of single-value "choose from a list" decisions
 * (mana color, protection color, keyword / creature-type / permanent-type / basic-land-type,
 * card name, text-change word, Abundance land/nonland, …). The specific variant lives in the
 * record's {@link PendingInteraction.ColorChoice#context()} and drives answer handling.
 *
 * <p>The prompt re-sends the exact begin-time {@code options} and {@code prompt} carried on the
 * record, so reconnect replay is byte-identical (the begin sites keep their own log lines, which
 * — matching the legacy replay — do not fire again on replay). The answer (the whole variant
 * dispatch) stays in {@link ChoiceHandlerService#handleListChoice}, which this handler delegates
 * to.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ColorChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ColorChoice> {

    private final SessionManager sessionManager;
    private final ChoiceHandlerService choiceHandlerService;

    @Override
    public Class<PendingInteraction.ColorChoice> handledType() {
        return PendingInteraction.ColorChoice.class;
    }

    @Override
    public AwaitingInput legacyInputType() {
        return AwaitingInput.COLOR_CHOICE;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.ColorChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.ColorChoice interaction, UUID recipientId) {
        sessionManager.sendToPlayer(recipientId,
                new ChooseFromListMessage(interaction.options(), interaction.prompt()));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.ColorChoice interaction,
                             InteractionAnswer answer) {
        String choice = ((InteractionAnswer.ListChoiceMade) answer).choice();
        choiceHandlerService.handleListChoice(gameData, player, choice);
    }
}
