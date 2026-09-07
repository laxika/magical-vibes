package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first offered card to shuffle for Emergent Ultimatum. */
class EmergentUltimatumOpponentChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EmergentUltimatumOpponentChoice> {

    @Override
    public Class<PendingInteraction.EmergentUltimatumOpponentChoice> handledType() {
        return PendingInteraction.EmergentUltimatumOpponentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EmergentUltimatumOpponentChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                List.of(interaction.validCardIds().getFirst())));
    }
}
