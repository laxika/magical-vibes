package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/** Returns every legal Aura from the AI's graveyard for Storm Herald's triggered ability. */
@Slf4j
class ReturnAurasFromGraveyardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ReturnAurasFromGraveyardChoice> {

    @Override
    public Class<PendingInteraction.ReturnAurasFromGraveyardChoice> handledType() {
        return PendingInteraction.ReturnAurasFromGraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ReturnAurasFromGraveyardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        log.info("AI: Returning {} Aura(s) from its graveyard in game {}",
                interaction.validCardIds().size(), ctx.gameId());
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(interaction.validCardIds()));
    }
}
