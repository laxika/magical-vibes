package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Answers "choose a card from the revealed hand" decisions: the AI picks the valid card with
 * the highest mana value (ported verbatim from the legacy {@code AiChoiceHandler} heuristic).
 */
@Slf4j
class RevealedHandChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.RevealedHandChoice> {

    @Override
    public Class<PendingInteraction.RevealedHandChoice> handledType() {
        return PendingInteraction.RevealedHandChoice.class;
    }

    @Override
    public void answer(PendingInteraction.RevealedHandChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.choosingPlayerId())) {
            return;
        }

        UUID targetPlayerId = interaction.targetPlayerId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> targetHand = ctx.gameData().playerHands.get(targetPlayerId);
        List<Integer> validIndices = interaction.validIndices();
        if (targetHand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> targetHand.get(i).getManaValue()))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing card {} from revealed hand in game {}", bestIndex, ctx.gameId());
        ctx.gameActions().handleCardChosen(ctx.selfConnection(), new CardChosenRequest(bestIndex));
    }
}
