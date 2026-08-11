package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/** Pays the maximum available X for an activated ability's variable graveyard-exile cost. */
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

        log.info("AI: Exiling {} graveyard cards for an activated ability in game {}",
                interaction.validCardIds().size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(interaction.validCardIds()));
    }
}
