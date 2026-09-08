package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

class HostileNegotiationsOpponentPileChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.HostileNegotiationsOpponentPileChoice> {

    @Override
    public Class<PendingInteraction.HostileNegotiationsOpponentPileChoice> handledType() {
        return PendingInteraction.HostileNegotiationsOpponentPileChoice.class;
    }

    @Override
    public void answer(PendingInteraction.HostileNegotiationsOpponentPileChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (ctx.aiPlayerId().equals(interaction.playerId())) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.MayAbilityChosen(true));
        }
    }
}
