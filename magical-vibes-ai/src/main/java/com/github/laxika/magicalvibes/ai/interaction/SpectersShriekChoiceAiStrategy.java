package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/** Chooses the highest-mana-value valid card from the targeted player's revealed hand. */
@Slf4j
class SpectersShriekChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.SpectersShriekChoice> {

    @Override
    public Class<PendingInteraction.SpectersShriekChoice> handledType() {
        return PendingInteraction.SpectersShriekChoice.class;
    }

    @Override
    public void answer(PendingInteraction.SpectersShriekChoice interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.choosingPlayerId())
                || interaction.validIndices().isEmpty()) {
            return;
        }

        List<Card> targetHand = ctx.gameData().playerHands
                .getOrDefault(interaction.targetPlayerId(), List.of());
        int chosenIndex = interaction.validIndices().stream()
                .filter(index -> index >= 0 && index < targetHand.size())
                .max(Comparator.comparingInt(index -> targetHand.get(index).getManaValue()))
                .orElse(-1);

        log.info("AI: Choosing card {} for Specter's Shriek in game {}", chosenIndex, ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardIndexChosen(chosenIndex));
    }
}
