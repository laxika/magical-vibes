package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesCreatureToBlockEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefendingPlayerChoosesCreatureToBlockEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DefendingPlayerChoosesCreatureToBlockEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defendingPlayerId = defendingPlayerId(gameData, entry.getAttackedTargetId());
        if (defendingPlayerId == null || entry.getSourcePermanentId() == null) {
            return;
        }

        List<UUID> creatureIds = untappedCreatureIds(gameData, defendingPlayerId);
        if (creatureIds.isEmpty()) {
            return;
        }

        if (creatureIds.size() == 1) {
            applyMustBlock(gameData, creatureIds.getFirst(), entry.getSourcePermanentId(), entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DefendingPlayerChoosesCreatureToBlock(
                        defendingPlayerId, entry.getSourcePermanentId(), entry.getCard().getName()));
        playerInputService.beginPermanentChoice(gameData, defendingPlayerId, creatureIds,
                entry.getCard().getName() + " — choose an untapped creature to block it.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.DefendingPlayerChoosesCreatureToBlock context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            return;
        }

        applyMustBlock(gameData, chosen.getId(), context.sourcePermanentId(), context.sourceCardName());
    }

    private List<UUID> untappedCreatureIds(GameData gameData, UUID playerId) {
        List<UUID> creatureIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (!permanent.isTapped() && gameQueryService.isCreature(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        }
        return creatureIds;
    }

    private void applyMustBlock(GameData gameData, UUID blockerId, UUID attackerId, String sourceCardName) {
        Permanent blocker = gameQueryService.findPermanentById(gameData, blockerId);
        if (blocker == null) {
            return;
        }

        blocker.getMustBlockIds().add(attackerId);
        gameLogService.append(gameData, GameLog.cardThen(
                blocker.getCard(), " must block " + sourceCardName + " this turn if able."));
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
