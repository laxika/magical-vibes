package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/**
 * Answers Struggle for Sanity's alternating hand exile. Both roles want the same card: the pile a
 * player builds is the pile that matters to them, so whoever is picking takes the
 * highest-mana-value card left — as the hand's owner to keep it, as the opponent to bin it.
 */
@Slf4j
class AlternatingHandExileChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.AlternatingHandExileChoice> {

    @Override
    public Class<PendingInteraction.AlternatingHandExileChoice> handledType() {
        return PendingInteraction.AlternatingHandExileChoice.class;
    }

    @Override
    public void answer(PendingInteraction.AlternatingHandExileChoice interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }

        List<Card> targetHand = ctx.gameData().playerHands.get(interaction.targetPlayerId());
        List<Integer> validIndices = interaction.validIndices();
        if (targetHand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        int chosenIndex = validIndices.stream()
                .filter(i -> i < targetHand.size())
                .max(Comparator.comparingInt(i -> targetHand.get(i).getManaValue()))
                .orElse(validIndices.get(0));

        log.info("AI: Exiling card {} in alternating hand exile in game {}", chosenIndex, ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
