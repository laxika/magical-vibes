package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControlledCreaturesEnterWithAdditionalCountersThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = ((ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect) effect).count();

        gameData.additionalEnterCountersThisTurn.merge(entry.getControllerId(), count, Integer::sum);

        gameLogService.append(gameData, GameLog.text(
                "Creatures entering this turn get " + count + " additional +1/+1 counter(s)."));
    }
}
