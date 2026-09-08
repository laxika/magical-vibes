package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

/** Chooses the first two offered creatures to be shuffled away. */
class EcologicalAppreciationOpponentChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EcologicalAppreciationOpponentChoice> {

    @Override
    public Class<PendingInteraction.EcologicalAppreciationOpponentChoice> handledType() {
        return PendingInteraction.EcologicalAppreciationOpponentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EcologicalAppreciationOpponentChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(
                interaction.cards().stream().limit(2).map(Card::getId).toList()));
    }
}
