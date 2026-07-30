package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/**
 * Always puts the exiled permanent card onto the battlefield: it is a free permanent and it keeps
 * Primal Surge's process going, so declining is never better.
 */
class ExiledPermanentPutOntoBattlefieldChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice> {

    @Override
    public Class<PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice> handledType() {
        return PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.MayAbilityChosen(true));
    }
}
