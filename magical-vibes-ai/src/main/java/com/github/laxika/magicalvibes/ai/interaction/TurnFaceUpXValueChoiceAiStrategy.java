package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

/** Chooses the largest payable X when turning a face-down permanent face up. */
@Slf4j
class TurnFaceUpXValueChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.TurnFaceUpXValueChoice> {

    @Override
    public Class<PendingInteraction.TurnFaceUpXValueChoice> handledType() {
        return PendingInteraction.TurnFaceUpXValueChoice.class;
    }

    @Override
    public void answer(PendingInteraction.TurnFaceUpXValueChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        ManaPool pool = ctx.gameData().playerManaPools.get(interaction.playerId());
        int payableX = pool == null ? 0 : new ManaCost(interaction.manaCost()).calculateMaxX(pool);
        int chosenValue = Math.min(interaction.maxValue(), payableX);

        log.info("AI: Choosing X={} to turn {} face up in game {}",
                chosenValue, interaction.cardName(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.NumberChosen(chosenValue));
    }
}
