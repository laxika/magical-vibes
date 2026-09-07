package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Reveals the highest-value card that can enter through Retraced Image. */
class RetracedImageCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.RetracedImageCardChoice> {

    @Override
    public Class<PendingInteraction.RetracedImageCardChoice> handledType() {
        return PendingInteraction.RetracedImageCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.RetracedImageCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.getOrDefault(interaction.playerId(), List.of());
        Set<String> permanentNames = ctx.gameData().playerBattlefields.values().stream()
                .flatMap(List::stream)
                .map(permanent -> permanent.getCard().getName())
                .collect(Collectors.toSet());
        int chosenIndex = interaction.validIndices().stream()
                .filter(index -> index >= 0 && index < hand.size())
                .max(Comparator
                        .comparing((Integer index) -> permanentNames.contains(hand.get(index).getName()))
                        .thenComparingInt(index -> hand.get(index).getManaValue()))
                .orElseGet(() -> interaction.validIndices().getFirst());

        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
