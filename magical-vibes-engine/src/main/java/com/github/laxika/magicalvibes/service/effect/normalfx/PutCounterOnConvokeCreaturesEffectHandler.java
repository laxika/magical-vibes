package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnConvokeCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnConvokeCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnConvokeCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnConvokeCreaturesEffect e = (PutCounterOnConvokeCreaturesEffect) effect;
        if (e.counterType() != CounterType.PLUS_ONE_PLUS_ONE) {
            throw new IllegalStateException("Convoke counter placement only supports +1/+1 counters");
        }

        List<Permanent> targets = new ArrayList<>();
        for (var permanentId : entry.getConvokeCreatureIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) {
                int placed = gameQueryService.replaceCounters(gameData, permanent,
                        CounterType.PLUS_ONE_PLUS_ONE, 1);
                if (placed > 0) {
                    permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                            permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                    permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                            gameData, permanent, placed);
                    targets.add(permanent);
                }
            }
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " puts a +1/+1 counter on " + targets.size() + " creature(s) that convoked it."));
        log.info("Game {} - {} puts +1/+1 counters on {} convoking creature(s)", gameData.id,
                entry.getCard().getName(), targets.size());

        for (Permanent target : targets) {
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, target);
        }
    }
}
