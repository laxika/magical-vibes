package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Passes rather than paying life to raise a Pain's Reward bid. */
class PainsRewardBidChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PainsRewardBidChoice> {

    @Override
    public Class<PendingInteraction.PainsRewardBidChoice> handledType() {
        return PendingInteraction.PainsRewardBidChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PainsRewardBidChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.NumberChosen(0));
    }
}
