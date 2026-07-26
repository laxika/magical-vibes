package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Puts back the required number of still-eligible cards instead of paying life. */
class SylvanLibraryChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.SylvanLibraryChoice> {

    @Override
    public Class<PendingInteraction.SylvanLibraryChoice> handledType() {
        return PendingInteraction.SylvanLibraryChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.SylvanLibraryChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        Set<UUID> handIds = ctx.gameData().playerHands
                .getOrDefault(interaction.playerId(), List.of())
                .stream()
                .map(card -> card.getId())
                .collect(java.util.stream.Collectors.toSet());
        List<UUID> toTop = interaction.drawnThisTurnCardIds().stream()
                .filter(handIds::contains)
                .limit(interaction.resolveCount())
                .toList();
        ctx.gameActions().answerInteraction(
                ctx.selfConnection(), new InteractionAnswer.CardsChosen(toTop));
    }
}
