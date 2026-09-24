package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
public class BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect) effect;
        List<Permanent> creatures = gameData.playerBattlefields.get(entry.getControllerId());
        if (creatures == null) {
            return;
        }

        for (Permanent creature : List.copyOf(creatures)) {
            if (!gameQueryService.isCreature(gameData, creature) || !creature.isAttacking()) {
                continue;
            }
            UUID attackedPlayerId = creature.getAttackTarget();
            if (attackedPlayerId == null || !gameData.playerIds.contains(attackedPlayerId)) {
                continue;
            }

            int defendingCreatureCount = countCreaturesControlledBy(gameData, attackedPlayerId);
            int powerBoost = defendingCreatureCount * boost.powerPerCreature();
            int toughnessBoost = defendingCreatureCount * boost.toughnessPerCreature();
            creature.setPowerModifier(creature.getPowerModifier() + powerBoost);
            creature.setToughnessModifier(creature.getToughnessModifier() + toughnessBoost);
            gameLogService.append(gameData, GameLog.builder()
                    .card(creature.getCard())
                    .text(String.format(" gets %+d/%+d until end of turn.", powerBoost, toughnessBoost))
                    .build());
            log.info("Game {} - {} gets {}/{}", gameData.id, creature.getCard().getName(),
                    powerBoost, toughnessBoost);
        }
    }

    private int countCreaturesControlledBy(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }
}
