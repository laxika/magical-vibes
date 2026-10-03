package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostOpponentCreaturesByPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostOpponentCreaturesByPoisonCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostOpponentCreaturesByPoisonCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int[] affectedCreatures = {0};

        gameData.forEachPermanent((controllerId, permanent) -> {
            if (entry.getControllerId().equals(controllerId)) {
                return;
            }
            int poisonCounters = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
            if (poisonCounters <= 0 || !gameQueryService.isCreature(gameData, permanent)) {
                return;
            }
            permanent.setPowerModifier(permanent.getPowerModifier() - poisonCounters);
            permanent.setToughnessModifier(permanent.getToughnessModifier() - poisonCounters);
            affectedCreatures[0]++;
        });

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" shrinks %d opponent creature(s) based on their controllers' poison counters until end of turn.",
                        affectedCreatures[0]))
                .build());
        log.info("Game {} - {} shrinks {} opponent creatures based on poison counters",
                gameData.id, entry.getCard().getName(), affectedCreatures[0]);
    }
}
