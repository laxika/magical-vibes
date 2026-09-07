package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;

/** Exiles the highest-mana-value eligible card to create the token copy. */
class ExileCardFromHandAndCreateTokenCopyChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExileCardFromHandAndCreateTokenCopyChoice> {

    @Override
    public Class<PendingInteraction.ExileCardFromHandAndCreateTokenCopyChoice> handledType() {
        return PendingInteraction.ExileCardFromHandAndCreateTokenCopyChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExileCardFromHandAndCreateTokenCopyChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.getOrDefault(interaction.playerId(), List.of());
        int chosenIndex = interaction.validIndices().stream()
                .filter(index -> index >= 0 && index < hand.size())
                .max(Comparator.comparingInt(index -> hand.get(index).getManaValue()))
                .orElse(-1);
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
