package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.GraveyardCardChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/**
 * Answers single-card graveyard choices: the AI picks the valid card with the highest mana
 * value (ported verbatim from the legacy {@code AiChoiceHandler} heuristic). Cross-graveyard
 * choices read the record's card pool; otherwise indices are into the AI's own graveyard.
 */
@Slf4j
class GraveyardChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.GraveyardChoice> {

    @Override
    public Class<PendingInteraction.GraveyardChoice> handledType() {
        return PendingInteraction.GraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.GraveyardChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Integer> validIndices = interaction.validIndices();
        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        List<Card> graveyard = interaction.cardPool();
        if (graveyard == null) {
            graveyard = ctx.gameData().playerGraveyards.getOrDefault(ctx.aiPlayerId(), List.of());
        }

        final List<Card> gy = graveyard;
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> i < gy.size() ? gy.get(i).getManaValue() : 0))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing graveyard card at index {} in game {}", bestIndex, ctx.gameId());
        ctx.gameActions().handleGraveyardCardChosen(ctx.selfConnection(), new GraveyardCardChosenRequest(bestIndex));
    }
}
