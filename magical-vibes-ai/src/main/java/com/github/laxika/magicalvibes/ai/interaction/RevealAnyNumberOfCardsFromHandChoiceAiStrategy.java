package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Chooses all legal cards when revealing cards from hand has a beneficial downstream effect. */
class RevealAnyNumberOfCardsFromHandChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.RevealAnyNumberOfCardsFromHandChoice> {

    @Override
    public Class<PendingInteraction.RevealAnyNumberOfCardsFromHandChoice> handledType() {
        return PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class;
    }

    @Override
    public void answer(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(interaction.validCardIds()));
    }
}
