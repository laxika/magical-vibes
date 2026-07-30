package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Answers Descent into Madness-style "exile N permanents you control and/or cards from your hand"
 * choices. The exile is mandatory, so the AI gives up what it values least: hand cards before
 * permanents, and within each group the lowest mana value first.
 */
@Slf4j
class ExilePermanentsOrHandCardsChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExilePermanentsOrHandCardsChoice> {

    @Override
    public Class<PendingInteraction.ExilePermanentsOrHandCardsChoice> handledType() {
        return PendingInteraction.ExilePermanentsOrHandCardsChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExilePermanentsOrHandCardsChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        int required = Math.min(interaction.count(), interaction.validCardIds().size());
        if (required <= 0) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.of()));
            return;
        }

        GameData gameData = ctx.gameData();
        Map<UUID, Integer> manaValues = new LinkedHashMap<>();
        List<UUID> handIds = new ArrayList<>();
        List<UUID> permanentIds = new ArrayList<>();

        for (Card card : gameData.playerHands.getOrDefault(ctx.aiPlayerId(), List.of())) {
            if (interaction.validCardIds().contains(card.getId())) {
                handIds.add(card.getId());
                manaValues.put(card.getId(), card.getManaValue());
            }
        }
        for (Permanent perm : gameData.playerBattlefields.getOrDefault(ctx.aiPlayerId(), List.of())) {
            UUID cardId = perm.getCard().getId();
            if (interaction.validCardIds().contains(cardId)) {
                permanentIds.add(cardId);
                manaValues.put(cardId, perm.getCard().getManaValue());
            }
        }

        handIds.sort((a, b) -> Integer.compare(manaValues.get(a), manaValues.get(b)));
        permanentIds.sort((a, b) -> Integer.compare(manaValues.get(a), manaValues.get(b)));

        List<UUID> chosen = new ArrayList<>(handIds);
        chosen.addAll(permanentIds);
        chosen = chosen.stream().limit(required).toList();
        if (chosen.size() < required) {
            // Zones drifted since the prompt — fall back to the begin-time list so the answer is legal.
            chosen = interaction.validCardIds().stream().limit(required).toList();
        }

        log.info("AI: Exiling {} object(s) for {} in game {}", chosen.size(), interaction.sourceName(),
                ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
