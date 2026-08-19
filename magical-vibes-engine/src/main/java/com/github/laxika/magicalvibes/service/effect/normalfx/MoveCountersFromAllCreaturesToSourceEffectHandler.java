package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromAllCreaturesToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class MoveCountersFromAllCreaturesToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCountersFromAllCreaturesToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        MoveCountersFromAllCreaturesToSourceEffect move =
                (MoveCountersFromAllCreaturesToSourceEffect) effect;
        int total = 0;
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                int count = permanent.getCounterCount(move.counterType());
                if (count > 0) {
                    permanent.setCounterCount(move.counterType(), 0);
                    if (move.counterType() == com.github.laxika.magicalvibes.model.CounterType.OIL) {
                        gameData.recordOilCounterRemoved(permanent, count);
                    }
                    total += count;
                }
            }
        }

        if (total > 0) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source,
                    move.counterType(), total);
            gameLogService.append(gameData, GameLog.builder()
                    .card(source.getCard())
                    .text(" moves all " + move.counterType() + " counters from creatures onto itself.")
                    .build());
        }
    }
}
