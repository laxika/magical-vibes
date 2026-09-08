package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.LinkedHashMap;
import java.util.List;

/** Selects four differently named cards from the offered library and outside-game pool. */
class TurtlesForeverSearchChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.TurtlesForeverSearchChoice> {

    @Override
    public Class<PendingInteraction.TurtlesForeverSearchChoice> handledType() {
        return PendingInteraction.TurtlesForeverSearchChoice.class;
    }

    @Override
    public void answer(PendingInteraction.TurtlesForeverSearchChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        LinkedHashMap<String, Card> byName = new LinkedHashMap<>();
        interaction.pool().forEach(card -> byName.putIfAbsent(card.getName(), card));
        List<java.util.UUID> chosen = byName.values().stream()
                .limit(4)
                .map(Card::getId)
                .toList();
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
