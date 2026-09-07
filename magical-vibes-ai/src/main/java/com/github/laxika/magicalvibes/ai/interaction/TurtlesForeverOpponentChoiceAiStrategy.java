package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Chooses two offered cards for the opposing controller to put into their hand. */
class TurtlesForeverOpponentChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.TurtlesForeverOpponentChoice> {

    @Override
    public Class<PendingInteraction.TurtlesForeverOpponentChoice> handledType() {
        return PendingInteraction.TurtlesForeverOpponentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.TurtlesForeverOpponentChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                interaction.cards().stream().limit(2).map(Card::getId).toList()));
    }
}
