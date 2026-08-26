package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/** Casts every spell copy offered by Eye of the Storm in the offered order. */
@Slf4j
class EyeOfTheStormCastChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EyeOfTheStormCastChoice> {

    @Override
    public Class<PendingInteraction.EyeOfTheStormCastChoice> handledType() {
        return PendingInteraction.EyeOfTheStormCastChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EyeOfTheStormCastChoice interaction,
                       AiInteractionContext context) throws Exception {
        if (!context.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        log.info("AI: Casting {} Eye of the Storm copies in game {}",
                interaction.validCopyIds().size(), context.gameId());
        context.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(interaction.validCopyIds()));
    }
}
