package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first available opponent to make Plargg and Nassari's card choice. */
class PlarggAndNassariOpponentChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PlarggAndNassariOpponentChoice> {

    @Override
    public Class<PendingInteraction.PlarggAndNassariOpponentChoice> handledType() {
        return PendingInteraction.PlarggAndNassariOpponentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PlarggAndNassariOpponentChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.controllerId()) || interaction.opponentIds().isEmpty()) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.PermanentsChosen(
                List.of(interaction.opponentIds().getFirst())));
    }
}
