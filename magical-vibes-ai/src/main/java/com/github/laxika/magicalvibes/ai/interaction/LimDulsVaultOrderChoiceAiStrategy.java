package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orders Lim-Dûl's Vault's held cards the same way as a plain library reorder: spells first
 * (cheapest to most expensive), lands last.
 */
class LimDulsVaultOrderChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.LimDulsVaultOrderChoice> {

    @Override
    public Class<PendingInteraction.LimDulsVaultOrderChoice> handledType() {
        return PendingInteraction.LimDulsVaultOrderChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.LimDulsVaultOrderChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Card> cards = interaction.cards();
        List<int[]> indexed = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int priority = card.hasType(CardType.LAND) ? 1000 + i : card.getManaValue();
            indexed.add(new int[]{i, priority});
        }
        indexed.sort(Comparator.comparingInt(entry -> entry[1]));

        ctx.gameActions().answerInteraction(
                new InteractionAnswer.CardOrder(indexed.stream().map(entry -> entry[0]).toList()));
    }
}
