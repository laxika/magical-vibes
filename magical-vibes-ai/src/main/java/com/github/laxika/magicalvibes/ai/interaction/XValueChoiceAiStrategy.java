package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers X value choices: the AI always chooses the maximum available X.
 */
@Slf4j
class XValueChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.XValueChoice> {

    @Override
    public Class<PendingInteraction.XValueChoice> handledType() {
        return PendingInteraction.XValueChoice.class;
    }

    @Override
    public void answer(PendingInteraction.XValueChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        int chosenValue = interaction.maxValue();
        log.info("AI: Choosing X={} for {} in game {}", chosenValue, interaction.cardName(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.NumberChosen(chosenValue));
    }
}
