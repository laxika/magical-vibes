package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Keeps the first offered card and puts the second on the bottom. */
class HandBottomExileChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.HandBottomExileChoice> {

    @Override
    public Class<PendingInteraction.HandBottomExileChoice> handledType() {
        return PendingInteraction.HandBottomExileChoice.class;
    }

    @Override
    public void answer(PendingInteraction.HandBottomExileChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId()) || interaction.cards().size() < 2) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.HandBottomExile(0, 1));
    }
}
