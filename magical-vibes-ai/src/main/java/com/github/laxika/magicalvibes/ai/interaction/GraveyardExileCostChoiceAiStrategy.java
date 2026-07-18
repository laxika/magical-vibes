package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/**
 * Answers the "exile a card from your graveyard as an activation cost" choice with the same
 * highest-mana-value heuristic the legacy {@code AiChoiceHandler} applied (this kind never
 * carries a card pool, so indices are into the AI's own graveyard).
 */
@Slf4j
class GraveyardExileCostChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.GraveyardExileCostChoice> {

    @Override
    public Class<PendingInteraction.GraveyardExileCostChoice> handledType() {
        return PendingInteraction.GraveyardExileCostChoice.class;
    }

    @Override
    public void answer(PendingInteraction.GraveyardExileCostChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Integer> validIndices = interaction.validIndices();
        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        final List<Card> gy = ctx.gameData().playerGraveyards.getOrDefault(ctx.aiPlayerId(), List.of());
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> i < gy.size() ? gy.get(i).getManaValue() : 0))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing graveyard card at index {} in game {}", bestIndex, ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.GraveyardCardChosen(bestIndex));
    }
}
