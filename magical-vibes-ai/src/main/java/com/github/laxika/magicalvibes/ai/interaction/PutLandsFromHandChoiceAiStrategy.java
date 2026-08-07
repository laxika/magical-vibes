package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/**
 * Puts every land card in hand onto the battlefield — free mana with no downside, and the
 * alternative is holding lands that The Great Aurora already refilled the hand with.
 */
class PutLandsFromHandChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.PutLandsFromHandChoice> {

    @Override
    public Class<PendingInteraction.PutLandsFromHandChoice> handledType() {
        return PendingInteraction.PutLandsFromHandChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.PutLandsFromHandChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardsChosen(interaction.validCardIds()));
    }
}
