package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers the X announcement for a cast for an alternative cost containing {X} (a miracle
 * cost like Entreat the Angels' {X}{W}{W}). The prompt's cap is potential-based, so the
 * answer is capped at the X the floating pool can actually cover on top of the cost's
 * coloured and generic parts — the engine re-prompts on a shortfall.
 */
@Slf4j
class AlternateCastXValueChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.AlternateCastXValueChoice> {

    @Override
    public Class<PendingInteraction.AlternateCastXValueChoice> handledType() {
        return PendingInteraction.AlternateCastXValueChoice.class;
    }

    @Override
    public void answer(PendingInteraction.AlternateCastXValueChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        ManaPool pool = ctx.gameData().playerManaPools.get(interaction.playerId());
        int payableX = pool == null ? 0 : new ManaCost(interaction.manaCost()).calculateMaxX(pool);
        int chosenValue = Math.min(interaction.maxValue(), payableX);

        log.info("AI: Choosing X={} for the {} cast of {} in game {}",
                chosenValue, interaction.costLabel(), interaction.cardName(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.NumberChosen(chosenValue));
    }
}
