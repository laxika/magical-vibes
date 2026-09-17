package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesPermanentsToExileEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the attack-triggered choice of permanents controlled by the defending player. */
@Component
@RequiredArgsConstructor
public class DefendingPlayerChoosesPermanentsToExileEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DefendingPlayerChoosesPermanentsToExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defendingPlayerId = defendingPlayerId(gameData, entry.getAttackedTargetId());
        if (defendingPlayerId == null) {
            return;
        }

        List<UUID> permanentIds = gameData.playerBattlefields
                .getOrDefault(defendingPlayerId, List.of())
                .stream()
                .map(Permanent::getId)
                .toList();
        if (permanentIds.isEmpty()) {
            return;
        }

        int requiredCount = Math.min(((DefendingPlayerChoosesPermanentsToExileEffect) effect).count(),
                permanentIds.size());
        if (permanentIds.size() <= requiredCount) {
            exilePermanents(gameData, permanentIds, entry.getCard().getName());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, defendingPlayerId, permanentIds,
                requiredCount,
                new MultiPermanentChoiceContext.DefendingPlayerChoosesPermanentsToExile(
                        defendingPlayerId, requiredCount, entry.getCard().getName()),
                entry.getCard().getName() + " — Choose " + requiredCount
                        + " permanents you control to exile.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.DefendingPlayerChoosesPermanentsToExile context) {
        exilePermanents(gameData, permanentIds, context.sourceCardName());
    }

    private void exilePermanents(GameData gameData, List<UUID> permanentIds, String sourceCardName) {
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                exileSupport.exilePermanentAndLog(gameData, permanent, sourceCardName);
            }
        }
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
