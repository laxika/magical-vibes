package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Selects the first four basic lands offered by Verdant Mastery. */
class VerdantMasterySearchChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.VerdantMasterySearchChoice> {

    @Override
    public Class<PendingInteraction.VerdantMasterySearchChoice> handledType() {
        return PendingInteraction.VerdantMasterySearchChoice.class;
    }

    @Override
    public void answer(PendingInteraction.VerdantMasterySearchChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                interaction.pool().stream().limit(4).map(Card::getId).toList()));
    }
}
