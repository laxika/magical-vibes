package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.LinkedHashMap;
import java.util.List;

/** Selects up to four differently named creatures for Ecological Appreciation. */
class EcologicalAppreciationSearchChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EcologicalAppreciationSearchChoice> {

    @Override
    public Class<PendingInteraction.EcologicalAppreciationSearchChoice> handledType() {
        return PendingInteraction.EcologicalAppreciationSearchChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EcologicalAppreciationSearchChoice interaction,
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
