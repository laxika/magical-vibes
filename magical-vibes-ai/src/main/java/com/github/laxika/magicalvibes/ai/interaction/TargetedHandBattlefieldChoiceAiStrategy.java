package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;

/** Chooses the highest-mana-value eligible card to put onto the battlefield. */
class TargetedHandBattlefieldChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.TargetedHandBattlefieldChoice> {

    @Override
    public Class<PendingInteraction.TargetedHandBattlefieldChoice> handledType() {
        return PendingInteraction.TargetedHandBattlefieldChoice.class;
    }

    @Override
    public void answer(PendingInteraction.TargetedHandBattlefieldChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.getOrDefault(interaction.targetPlayerId(), List.of());
        int chosenIndex = interaction.validIndices().stream()
                .filter(index -> index >= 0 && index < hand.size())
                .max(Comparator.comparingInt(index -> hand.get(index).getManaValue()))
                .orElse(-1);
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
