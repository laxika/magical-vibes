package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackTargetPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetCreatureMustAttackTargetPlayerThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureMustAttackTargetPlayerThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureMustAttackTargetPlayerThisTurnEffect) effect;
        List<UUID> creatureTargets = entry.targetsForGroup(e.creatureTargetGroup());
        List<UUID> playerTargets = entry.targetsForGroup(e.playerTargetGroup());
        if (creatureTargets.isEmpty() || playerTargets.isEmpty()) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, creatureTargets.getFirst());
        UUID playerId = playerTargets.getFirst();
        if (creature == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        creature.setMustAttackThisTurn(true);
        creature.setMustAttackTargetId(playerId);

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                " must attack " + (playerName == null ? "that player" : playerName) + " this turn if able."));
        log.info("Game {} - {} must attack {} this turn if able", gameData.id,
                creature.getCard().getName(), playerName);
    }
}
