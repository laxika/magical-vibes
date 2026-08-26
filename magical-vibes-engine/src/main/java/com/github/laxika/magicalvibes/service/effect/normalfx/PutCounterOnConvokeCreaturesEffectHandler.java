package com.github.laxika.magicalvibes.service.effect.normalfx;

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

        List<Permanent> targets = new ArrayList<>();
        for (var permanentId : entry.getConvokeCreatureIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null
                    && permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, permanent, e.counterType(), 1) > 0) {
                targets.add(permanent);
            }
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " puts a " + permanentCounterSupport.counterTypeName(e.counterType()) + " counter on "
                        + targets.size() + " creature(s) that convoked it."));
        log.info("Game {} - {} puts {} counters on {} convoking creature(s)", gameData.id,
                entry.getCard().getName(), e.counterType(), targets.size());

    }
}
