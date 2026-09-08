package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;

/** Chooses the highest-value card not already preserved for another color when possible. */
class EachPlayerChoosesOneCardOfEachColorChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice> {

    @Override
    public Class<PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice> handledType() {
        return PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.getOrDefault(interaction.playerId(), List.of());
        Comparator<Integer> byManaValue = Comparator.comparingInt(index -> hand.get(index).getManaValue());
        int chosenIndex = interaction.validIndices().stream()
                .filter(index -> index >= 0 && index < hand.size())
                .filter(index -> !interaction.chosenCardIds().contains(hand.get(index).getId()))
                .max(byManaValue)
                .orElseGet(() -> interaction.validIndices().stream()
                        .filter(index -> index >= 0 && index < hand.size())
                        .max(byManaValue)
                        .orElseThrow(() -> new IllegalStateException("No valid card index for color choice")));

        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
