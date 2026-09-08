package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Selects the first legal lands for the current Verdant Mastery distribution step. */
class VerdantMasteryLandChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.VerdantMasteryLandChoice> {

    @Override
    public Class<PendingInteraction.VerdantMasteryLandChoice> handledType() {
        return PendingInteraction.VerdantMasteryLandChoice.class;
    }

    @Override
    public void answer(PendingInteraction.VerdantMasteryLandChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        int count = interaction.chooseForOpponent() ? 1 : Math.min(2, interaction.cards().size());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                interaction.cards().stream().limit(count).map(Card::getId).toList()));
    }
}
