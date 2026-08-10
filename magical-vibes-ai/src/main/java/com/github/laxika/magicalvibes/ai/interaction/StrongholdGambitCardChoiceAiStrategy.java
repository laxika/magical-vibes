package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/** Answers Stronghold Gambit's mandatory hidden hand-card choice. */
@Slf4j
class StrongholdGambitCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.StrongholdGambitCardChoice> {

    @Override
    public Class<PendingInteraction.StrongholdGambitCardChoice> handledType() {
        return PendingInteraction.StrongholdGambitCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.StrongholdGambitCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.get(interaction.playerId());
        List<Integer> validIndices = interaction.validIndices();
        if (hand == null || validIndices.isEmpty()) {
            return;
        }

        int chosenIndex = validIndices.stream()
                .filter(index -> index >= 0 && index < hand.size())
                .max(Comparator.comparingInt(index -> hand.get(index).getManaValue()))
                .orElse(validIndices.get(0));

        log.info("AI: Choosing card {} for Stronghold Gambit in game {}", chosenIndex, ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
