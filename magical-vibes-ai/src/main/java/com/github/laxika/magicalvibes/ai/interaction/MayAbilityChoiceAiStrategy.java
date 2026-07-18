package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers may-ability choices: the baseline AI always accepts. Hard AI overrides
 * {@code AiDecisionEngine.handleMayAbilityChoice} with a board-aware evaluation and
 * only falls back here when the choice is not its own.
 */
@Slf4j
class MayAbilityChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.MayAbilityChoice> {

    @Override
    public Class<PendingInteraction.MayAbilityChoice> handledType() {
        return PendingInteraction.MayAbilityChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MayAbilityChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        log.info("AI: Accepting may ability in game {}", ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.MayAbilityChosen(true));
    }
}
