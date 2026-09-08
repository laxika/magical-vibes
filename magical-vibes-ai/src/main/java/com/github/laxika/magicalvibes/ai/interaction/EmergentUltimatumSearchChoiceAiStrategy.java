package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.LinkedHashMap;
import java.util.List;

/** Selects up to three differently named monocolored cards for Emergent Ultimatum. */
class EmergentUltimatumSearchChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.EmergentUltimatumSearchChoice> {

    @Override
    public Class<PendingInteraction.EmergentUltimatumSearchChoice> handledType() {
        return PendingInteraction.EmergentUltimatumSearchChoice.class;
    }

    @Override
    public void answer(PendingInteraction.EmergentUltimatumSearchChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        LinkedHashMap<String, Card> byName = new LinkedHashMap<>();
        interaction.pool().forEach(card -> byName.putIfAbsent(card.getName(), card));
        List<java.util.UUID> chosen = byName.values().stream()
                .limit(3)
                .map(Card::getId)
                .toList();
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
