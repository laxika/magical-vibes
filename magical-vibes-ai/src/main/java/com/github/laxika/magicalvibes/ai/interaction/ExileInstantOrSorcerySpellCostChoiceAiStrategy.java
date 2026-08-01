package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;

/** Chooses the first instant or sorcery spell available for an activation cost. */
class ExileInstantOrSorcerySpellCostChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExileInstantOrSorcerySpellCostChoice> {

    @Override
    public Class<PendingInteraction.ExileInstantOrSorcerySpellCostChoice> handledType() {
        return PendingInteraction.ExileInstantOrSorcerySpellCostChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExileInstantOrSorcerySpellCostChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        List<java.util.UUID> valid = interaction.validCardIds();
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                valid.isEmpty() ? List.of() : List.of(valid.getFirst())));
    }
}
