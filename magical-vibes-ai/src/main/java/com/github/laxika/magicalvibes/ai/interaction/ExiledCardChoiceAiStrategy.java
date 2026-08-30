package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first eligible exiled card for a mandatory return choice. */
class ExiledCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExiledCardChoice> {

    @Override
    public Class<PendingInteraction.ExiledCardChoice> handledType() {
        return PendingInteraction.ExiledCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExiledCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                interaction.validCardIds().isEmpty()
                        ? List.of()
                        : List.of(interaction.validCardIds().getFirst())));
    }
}
