package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/** Pays an activated ability's graveyard-exile cost with a legal maximum selection. */
@Slf4j
class ActivatedAbilityGraveyardExileCostChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ActivatedAbilityGraveyardExileCostChoice> {

    @Override
    public Class<PendingInteraction.ActivatedAbilityGraveyardExileCostChoice> handledType() {
        return PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> selectedCardIds = interaction.validCardIds().subList(
                0, Math.min(interaction.maximumCards(), interaction.validCardIds().size()));
        log.info("AI: Exiling {} graveyard cards for an activated ability in game {}",
                selectedCardIds.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(selectedCardIds));
    }
}
