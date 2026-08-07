package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackSourcePermanentNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Records the delayed single-creature taunt. The ability is an "up to one target" one, so an
 * activation with no target resolves as a no-op.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetCreatureMustAttackSourcePermanentNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureMustAttackSourcePermanentNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        List<UUID> targetIds = entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()
                ? entry.getTargetIds()
                : entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            gameData.creatureMustAttackPermanentNextTurn.put(target.getId(), sourcePermanentId);
            gameLogService.append(gameData, GameLog.builder()
                    .card(target.getCard())
                    .text(" attacks " + source.getCard().getName() + " during its controller's next turn if able.")
                    .build());
            log.info("Game {} - {} must attack {} during its controller's next turn",
                    gameData.id, target.getCard().getName(), source.getCard().getName());
        }
    }
}
