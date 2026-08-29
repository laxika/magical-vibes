package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

class HostileNegotiationsFaceUpChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.HostileNegotiationsFaceUpChoice> {

    @Override
    public Class<PendingInteraction.HostileNegotiationsFaceUpChoice> handledType() {
        return PendingInteraction.HostileNegotiationsFaceUpChoice.class;
    }

    @Override
    public void answer(PendingInteraction.HostileNegotiationsFaceUpChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (ctx.aiPlayerId().equals(interaction.playerId())) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.MayAbilityChosen(true));
        }
    }
}
