package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Puts the maximum offered number of matching cards onto the battlefield. */
class PutUpToCardsFromHandOntoBattlefieldChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice> {

    @Override
    public Class<PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice> handledType() {
        return PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                interaction.validCardIds().stream().limit(interaction.maxCount()).toList()));
    }
}
