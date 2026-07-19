package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers X value choices: the AI always chooses the maximum available X. For mana
 * payments the max is potential-based (the prompt allows tapping sources while it is
 * open), so the answer is capped at what is actually floating — the engine charges the
 * pool as it stands and would re-prompt on a shortfall. The decision engine floats
 * spare mana before dispatching here ({@code AiDecisionEngine.handleXValueChoice}).
 */
@Slf4j
class XValueChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.XValueChoice> {

    @Override
    public Class<PendingInteraction.XValueChoice> handledType() {
        return PendingInteraction.XValueChoice.class;
    }

    @Override
    public void answer(PendingInteraction.XValueChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        int chosenValue = interaction.maxValue();
        if (interaction.manaPayment()) {
            ManaPool pool = ctx.gameData().playerManaPools.get(interaction.playerId());
            int payable = pool == null ? 0
                    : pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
            chosenValue = Math.min(chosenValue, payable);
        }
        log.info("AI: Choosing X={} for {} in game {}", chosenValue, interaction.cardName(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.NumberChosen(chosenValue));
    }
}
