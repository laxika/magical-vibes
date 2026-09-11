package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.InteractionOptions;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Selects offered cards, declining an optional choice when its minimum selection is unavailable. */
record OfferedCardsChoiceAiStrategy<T extends PendingInteraction>(Class<T> handledType, int minimumSelectionSize)
        implements AiInteractionStrategy<T> {

    @Override
    public void answer(T interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }
        var options = (InteractionOptions.MultiCardPick) interaction.legalOptions();
        int count = Math.min(options.maxCount(), options.validCardIds().size());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                count < minimumSelectionSize ? List.of() : options.validCardIds().subList(0, count)));
    }
}
