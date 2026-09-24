package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceMustAttackRandomOpponentThisCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class SourceMustAttackRandomOpponentThisCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceMustAttackRandomOpponentThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID controllerId = entry.getControllerId();
        Permanent source = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || controllerId == null) {
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !controllerId.equals(playerId))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        UUID opponentId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        source.setMustAttackThisCombat(true);
        source.setMustAttackTargetId(opponentId);

        String opponentName = gameData.playerIdToName.getOrDefault(opponentId, "an opponent");
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                " must attack " + opponentName + " this combat if able."));
        log.info("Game {} - {} must attack {} this combat if able", gameData.id,
                source.getCard().getName(), opponentName);
    }
}
