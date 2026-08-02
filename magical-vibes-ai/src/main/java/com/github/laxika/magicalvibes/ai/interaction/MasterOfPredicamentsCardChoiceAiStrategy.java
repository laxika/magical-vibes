package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/**
 * Answers Master of Predicaments' hand-card separation. The guessing player sees nothing about the
 * chosen card, so no pick is better at dodging the guess; the AI instead maximises the payoff of a
 * wrong guess and separates the highest-mana-value card, which is the most valuable free cast.
 */
@Slf4j
class MasterOfPredicamentsCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.MasterOfPredicamentsCardChoice> {

    @Override
    public Class<PendingInteraction.MasterOfPredicamentsCardChoice> handledType() {
        return PendingInteraction.MasterOfPredicamentsCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MasterOfPredicamentsCardChoice interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.get(interaction.playerId());
        List<Integer> validIndices = interaction.validIndices();
        if (hand == null || validIndices.isEmpty()) {
            return;
        }

        int chosenIndex = validIndices.stream()
                .filter(i -> i < hand.size())
                .max(Comparator.comparingInt(i -> hand.get(i).getManaValue()))
                .orElse(validIndices.get(0));

        log.info("AI: Separating card {} for Master of Predicaments in game {}", chosenIndex, ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
