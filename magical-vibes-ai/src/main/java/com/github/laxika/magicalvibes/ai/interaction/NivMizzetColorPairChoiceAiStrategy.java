package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Chooses the highest-mana-value eligible card for each represented two-color pair. */
class NivMizzetColorPairChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.NivMizzetColorPairChoice> {

    @Override
    public Class<PendingInteraction.NivMizzetColorPairChoice> handledType() {
        return PendingInteraction.NivMizzetColorPairChoice.class;
    }

    @Override
    public void answer(PendingInteraction.NivMizzetColorPairChoice interaction,
                       AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        Map<Set<CardColor>, Card> bestByPair = new LinkedHashMap<>();
        for (Card card : interaction.revealedCards()) {
            if (!interaction.validCardIds().contains(card.getId())) {
                continue;
            }
            Set<CardColor> colors = ctx.gameQueryService().getEffectiveCardColors(ctx.gameData(), card);
            if (colors.size() != 2) {
                continue;
            }
            bestByPair.merge(Set.copyOf(colors), card,
                    java.util.function.BinaryOperator.maxBy(Comparator.comparingInt(Card::getManaValue)));
        }

        List<UUID> chosenIds = bestByPair.values().stream()
                .map(Card::getId)
                .limit(interaction.requiredPairCount())
                .toList();
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosenIds));
    }
}
