package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Conservatively stops after Forbidden Ritual's mandatory first cycle. */
class ForbiddenRitualRepeatChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ForbiddenRitualRepeatChoice> {

    @Override
    public Class<PendingInteraction.ForbiddenRitualRepeatChoice> handledType() {
        return PendingInteraction.ForbiddenRitualRepeatChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.ForbiddenRitualRepeatChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.MayAbilityChosen(false));
    }
}
