package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers X value choices: the AI always chooses the maximum available X. For mana
 * payments the max is potential-based (the prompt allows tapping sources while it is
 * open), so the answer is capped at what is actually floating. When the prompt carries
 * a full mana cost, that cost determines the maximum legal X so fixed colored components
 * are reserved. The decision engine floats spare mana before dispatching here
 * ({@code AiDecisionEngine.handleXValueChoice}).
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

        int chosenValue = interaction.maxValue() == Integer.MAX_VALUE
                ? interaction.minValue() : interaction.maxValue();
        if (interaction.manaPayment()) {
            ManaPool pool = ctx.gameData().playerManaPools.get(interaction.playerId());
            if (pool == null) {
                chosenValue = 0;
            } else if (interaction.manaCost() != null) {
                chosenValue = Math.min(chosenValue, new ManaCost(interaction.manaCost()).calculateMaxX(pool));
            } else {
                int payable = pool.getTotal() + pool.getArtifactOnlyColorless()
                        + pool.getMyrOnlyColorless() + pool.getXCostOnlyColorless();
                chosenValue = Math.min(chosenValue, payable);
            }
        }
        log.info("AI: Choosing X={} for {} in game {}", chosenValue, interaction.cardName(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.NumberChosen(chosenValue));
    }
}
