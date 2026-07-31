package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Conservatively keeps the first five cards Lim-Dûl's Vault shows rather than paying life. */
class LimDulsVaultRepeatChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.LimDulsVaultRepeatChoice> {

    @Override
    public Class<PendingInteraction.LimDulsVaultRepeatChoice> handledType() {
        return PendingInteraction.LimDulsVaultRepeatChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.LimDulsVaultRepeatChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.MayAbilityChosen(false));
    }
}
