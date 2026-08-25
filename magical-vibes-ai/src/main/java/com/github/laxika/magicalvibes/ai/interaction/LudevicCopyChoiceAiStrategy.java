package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first eligible creature card exiled with Ludevic. */
class LudevicCopyChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.LudevicCopyChoice> {

    @Override
    public Class<PendingInteraction.LudevicCopyChoice> handledType() {
        return PendingInteraction.LudevicCopyChoice.class;
    }

    @Override
    public void answer(PendingInteraction.LudevicCopyChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.validCardIds().isEmpty()) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                List.of(interaction.validCardIds().getFirst())));
    }
}
