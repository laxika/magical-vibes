package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first available opponent to make Emergent Ultimatum's card choice. */
class EmergentUltimatumOpponentSelectionChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EmergentUltimatumOpponentSelectionChoice> {

    @Override
    public Class<PendingInteraction.EmergentUltimatumOpponentSelectionChoice> handledType() {
        return PendingInteraction.EmergentUltimatumOpponentSelectionChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EmergentUltimatumOpponentSelectionChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.controllerId()) || interaction.opponentIds().isEmpty()) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.PermanentsChosen(
                List.of(interaction.opponentIds().getFirst())));
    }
}
