package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Conservatively stops after Ad Nauseam's mandatory first reveal. */
class AdNauseamRepeatChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.AdNauseamRepeatChoice> {

    @Override
    public Class<PendingInteraction.AdNauseamRepeatChoice> handledType() {
        return PendingInteraction.AdNauseamRepeatChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.AdNauseamRepeatChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.MayAbilityChosen(false));
    }
}
