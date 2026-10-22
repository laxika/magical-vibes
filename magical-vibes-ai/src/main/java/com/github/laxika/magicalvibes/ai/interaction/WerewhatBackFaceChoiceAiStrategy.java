package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Keeps the exiled double-faced card's front face as Werewhat's back face. */
class WerewhatBackFaceChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.WerewhatBackFaceChoice> {

    @Override
    public Class<PendingInteraction.WerewhatBackFaceChoice> handledType() {
        return PendingInteraction.WerewhatBackFaceChoice.class;
    }

    @Override
    public void answer(PendingInteraction.WerewhatBackFaceChoice interaction, AiInteractionContext ctx) {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.ListChoiceMade(
                interaction.options().getFirst()));
    }
}
