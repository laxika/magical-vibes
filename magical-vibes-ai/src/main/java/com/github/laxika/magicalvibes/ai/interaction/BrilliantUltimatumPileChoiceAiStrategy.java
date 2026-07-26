package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.List;
import java.util.UUID;

/** Chooses the Brilliant Ultimatum pile with the greater total mana value. */
class BrilliantUltimatumPileChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.BrilliantUltimatumPileChoice> {

    @Override
    public Class<PendingInteraction.BrilliantUltimatumPileChoice> handledType() {
        return PendingInteraction.BrilliantUltimatumPileChoice.class;
    }

    @Override
    public void answer(
            PendingInteraction.BrilliantUltimatumPileChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        boolean choosePileOne =
                pileValue(interaction.pile1CardIds(), ctx) >= pileValue(interaction.pile2CardIds(), ctx);
        ctx.gameActions().answerInteraction(
                ctx.selfConnection(), new InteractionAnswer.MayAbilityChosen(choosePileOne));
    }

    private int pileValue(List<UUID> cardIds, AiInteractionContext ctx) {
        return cardIds.stream()
                .map(ctx.gameData()::findExiledCard)
                .filter(java.util.Objects::nonNull)
                .map(ExiledCardEntry::card)
                .mapToInt(card -> Math.max(1, card.getManaValue()))
                .sum();
    }
}
