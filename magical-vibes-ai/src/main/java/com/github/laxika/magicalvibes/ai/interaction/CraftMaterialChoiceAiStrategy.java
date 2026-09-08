package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/** Pays a craft material cost with the first legal selection. */
@Slf4j
class CraftMaterialChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.CraftMaterialChoice> {

    @Override
    public Class<PendingInteraction.CraftMaterialChoice> handledType() {
        return PendingInteraction.CraftMaterialChoice.class;
    }

    @Override
    public void answer(PendingInteraction.CraftMaterialChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        int selectionSize = Math.min(interaction.maximumCards(), interaction.validCardIds().size());
        List<UUID> selectedCardIds = interaction.validCardIds().subList(0, selectionSize);
        log.info("AI: Exiling {} craft material cards in game {}", selectedCardIds.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(selectedCardIds));
    }
}
