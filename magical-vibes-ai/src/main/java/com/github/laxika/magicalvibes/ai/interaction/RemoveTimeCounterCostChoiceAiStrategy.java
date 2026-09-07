package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Pays the mixed time-counter cost with the first legal permanent or suspended card. */
class RemoveTimeCounterCostChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.RemoveTimeCounterCostChoice> {

    @Override
    public Class<PendingInteraction.RemoveTimeCounterCostChoice> handledType() {
        return PendingInteraction.RemoveTimeCounterCostChoice.class;
    }

    @Override
    public void answer(PendingInteraction.RemoveTimeCounterCostChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.validCardIds().isEmpty()) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                List.of(interaction.validCardIds().getFirst())));
    }
}
