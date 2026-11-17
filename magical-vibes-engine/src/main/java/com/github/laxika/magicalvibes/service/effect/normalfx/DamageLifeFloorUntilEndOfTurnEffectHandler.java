package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DamageLifeFloorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageLifeFloorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var floorEffect = (DamageLifeFloorUntilEndOfTurnEffect) effect;
        if (entry.getControllerId() != null) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), floorEffect, entry.getControllerId(),
                    Duration.UNTIL_END_OF_TURN));
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": damage can't reduce its controller's life total below " + floorEffect.floor() + " this turn."));
    }
}
