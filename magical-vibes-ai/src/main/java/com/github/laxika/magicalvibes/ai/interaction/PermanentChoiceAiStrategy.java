package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.PermanentChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Answers single permanent / any-target picks: the AI prefers the opponent's strongest
 * creature (by effective power), then the opponent's highest-mana-value permanent, then its
 * own cheapest permanent, then the first valid ID. Ported verbatim from the legacy
 * {@code AiChoiceHandler.handlePermanentChoice} heuristic.
 */
@Slf4j
class PermanentChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.PermanentChoice> {

    @Override
    public Class<PendingInteraction.PermanentChoice> handledType() {
        return PendingInteraction.PermanentChoice.class;
    }

    @Override
    public void answer(PendingInteraction.PermanentChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        Set<UUID> validIds = interaction.validIds();
        if (validIds.isEmpty()) {
            return;
        }

        GameData gameData = ctx.gameData();
        UUID opponentId = null;
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(ctx.aiPlayerId())) {
                opponentId = id;
                break;
            }
        }
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> ownField = gameData.playerBattlefields.getOrDefault(ctx.aiPlayerId(), List.of());

        // Try opponent's best creature first
        UUID best = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .filter(p -> ctx.gameQueryService().isCreature(gameData, p))
                .max(Comparator.comparingInt(p -> ctx.gameQueryService().getEffectivePower(gameData, p)))
                .map(Permanent::getId)
                .orElse(null);

        if (best == null) {
            best = opponentField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(null);
        }

        if (best == null) {
            best = ownField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .min(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(validIds.iterator().next());
        }

        log.info("AI: Choosing permanent {} in game {}", best, ctx.gameId());
        ctx.gameActions().handlePermanentChosen(ctx.selfConnection(), new PermanentChosenRequest(best));
    }
}
