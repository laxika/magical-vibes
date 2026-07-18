package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Answers multi-permanent selections: the AI prefers the opponent's strongest permanents
 * (by effective power), falling back to the first valid IDs when none are the opponent's.
 */
@Slf4j
class MultiPermanentChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.MultiPermanentChoice> {

    @Override
    public Class<PendingInteraction.MultiPermanentChoice> handledType() {
        return PendingInteraction.MultiPermanentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MultiPermanentChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> validIds = interaction.validIds();
        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        GameData gameData = ctx.gameData();
        List<Permanent> opponentField = gameData.getOpponentBattlefield(ctx.aiPlayerId());

        List<UUID> chosen = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .sorted(Comparator.comparingInt((Permanent p) -> ctx.gameQueryService().getEffectivePower(gameData, p)).reversed())
                .limit(interaction.maxCount())
                .map(Permanent::getId)
                .toList();

        if (chosen.isEmpty()) {
            chosen = validIds.stream().limit(interaction.maxCount()).toList();
        }

        log.info("AI: Choosing {} permanents in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.PermanentsChosen(chosen));
    }
}
