package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.InteractionOptions;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Chooses the first legal hand-card index for a mandatory card-index prompt. */
record OfferedCardIndexChoiceAiStrategy<T extends PendingInteraction>(Class<T> handledType)
        implements AiInteractionStrategy<T> {

    @Override
    public void answer(T interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }
        var options = (InteractionOptions.CardIndexPick) interaction.legalOptions();
        if (options.validIndices().isEmpty()) {
            if (options.declinable()) {
                ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(-1));
            }
            return;
        }
        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardIndexChosen(options.validIndices().getFirst()));
    }
}
