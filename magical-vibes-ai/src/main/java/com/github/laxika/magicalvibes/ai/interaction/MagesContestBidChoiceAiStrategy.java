package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Passes rather than paying life to raise a Mages' Contest bid. */
class MagesContestBidChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.MagesContestBidChoice> {

    @Override
    public Class<PendingInteraction.MagesContestBidChoice> handledType() {
        return PendingInteraction.MagesContestBidChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MagesContestBidChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.NumberChosen(0));
    }
}
