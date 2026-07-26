package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles the COLOR_CHOICE protocol family of single-value "choose from a list" decisions
 * (mana color, protection color, keyword / creature-type / permanent-type / basic-land-type,
 * card name, text-change word, Abundance land/nonland, …). The specific variant lives in the
 * record's {@link PendingInteraction.ColorChoice#context()} and drives answer handling.
 *
 * <p>The prompt re-sends the exact begin-time {@code options} and {@code prompt} carried on the
 * record, so reconnect projection is byte-identical. Begin sites keep their own log lines, which
 * do not fire again on reconnect. The whole answer-variant dispatch stays in
 * {@link ChoiceHandlerService#handleListChoice}, which this handler delegates to.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ColorChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ColorChoice> {

    private final ChoiceHandlerService choiceHandlerService;

    @Override
    public Class<PendingInteraction.ColorChoice> handledType() {
        return PendingInteraction.ColorChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.ColorChoice interaction,
                             InteractionAnswer answer) {
        String choice = ((InteractionAnswer.ListChoiceMade) answer).choice();
        choiceHandlerService.handleListChoice(gameData, player, choice);
    }
}
