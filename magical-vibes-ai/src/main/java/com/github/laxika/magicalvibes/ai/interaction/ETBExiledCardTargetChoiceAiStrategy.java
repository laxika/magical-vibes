package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first legal face-up exiled card or permanent for an enter-the-battlefield trigger. */
class ETBExiledCardTargetChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ETBExiledCardTargetChoice> {

    @Override
    public Class<PendingInteraction.ETBExiledCardTargetChoice> handledType() {
        return PendingInteraction.ETBExiledCardTargetChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ETBExiledCardTargetChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.controllerId())
                || (interaction.validCardIds().isEmpty() && interaction.validPermanentIds().isEmpty())) {
            return;
        }
        var targetId = interaction.validCardIds().isEmpty()
                ? interaction.validPermanentIds().getFirst()
                : interaction.validCardIds().getFirst();
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                List.of(targetId)));
    }
}
