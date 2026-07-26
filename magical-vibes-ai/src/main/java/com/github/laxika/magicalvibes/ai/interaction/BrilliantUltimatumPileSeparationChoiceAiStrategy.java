package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;
import java.util.UUID;

/** Splits the offered cards roughly evenly into Brilliant Ultimatum's first pile. */
class BrilliantUltimatumPileSeparationChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.BrilliantUltimatumPileSeparationChoice> {

    @Override
    public Class<PendingInteraction.BrilliantUltimatumPileSeparationChoice> handledType() {
        return PendingInteraction.BrilliantUltimatumPileSeparationChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.BrilliantUltimatumPileSeparationChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        int pileOneSize = interaction.validCardIds().size() / 2;
        List<UUID> pileOne = interaction.validCardIds().stream().limit(pileOneSize).toList();
        ctx.gameActions().answerInteraction(
                ctx.selfConnection(), new InteractionAnswer.CardsChosen(pileOne));
    }
}
