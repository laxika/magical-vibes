package com.github.laxika.magicalvibes.service.effect.normalfx;

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
        gameData.damageLifeFloorsUntilEndOfTurn.merge(
                entry.getControllerId(), floorEffect.floor(), Math::max);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": damage can't reduce its controller's life total below " + floorEffect.floor() + " this turn."));
    }
}
