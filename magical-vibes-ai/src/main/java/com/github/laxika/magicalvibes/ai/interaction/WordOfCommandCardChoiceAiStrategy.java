package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/** Answers Word of Command's mandatory choice from the revealed opponent hand. */
@Slf4j
class WordOfCommandCardChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.WordOfCommandCardChoice> {

    @Override
    public Class<PendingInteraction.WordOfCommandCardChoice> handledType() {
        return PendingInteraction.WordOfCommandCardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.WordOfCommandCardChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.decidingPlayerId())) {
            return;
        }

        List<Card> hand = ctx.gameData().playerHands.get(interaction.targetPlayerId());
        List<Integer> validIndices = interaction.validIndices();
        if (hand == null || validIndices.isEmpty()) {
            return;
        }

        int chosenIndex = validIndices.stream()
                .filter(index -> index >= 0 && index < hand.size())
                .max(Comparator.comparingInt(index -> hand.get(index).getManaValue()))
                .orElse(validIndices.get(0));

        log.info("AI: Choosing card {} for Word of Command in game {}", chosenIndex, ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
