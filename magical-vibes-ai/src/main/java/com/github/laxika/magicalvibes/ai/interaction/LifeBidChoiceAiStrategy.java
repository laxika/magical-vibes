package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers a life bid ({@link PendingInteraction.LifeBidChoice}) by bidding 0: whatever the prize, the AI doesn't lose
 * life to top the high bid. As the controller it opens at 0 and wins the prize when every other bidder passes too.
 */
@Slf4j
class LifeBidChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.LifeBidChoice> {

    @Override
    public Class<PendingInteraction.LifeBidChoice> handledType() {
        return PendingInteraction.LifeBidChoice.class;
    }

    @Override
    public void answer(PendingInteraction.LifeBidChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        log.info("AI: Bidding 0 life on {} ({}) in game {}", interaction.cardName(), interaction.prize(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.NumberChosen(0));
    }
}
