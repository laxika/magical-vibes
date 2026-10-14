package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedControllerDamageMultiplication;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyControllerDamageToOpponentsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultiplyControllerDamageToOpponentsThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MultiplyControllerDamageToOpponentsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var multiplication = (MultiplyControllerDamageToOpponentsThisTurnEffect) effect;
        gameData.queueDelayedAction(new DelayedControllerDamageMultiplication(
                entry.getControllerId(), multiplication.multiplier(), false));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": damage dealt to opponents by sources you control is multiplied by "
                        + multiplication.multiplier() + " this turn."));
    }
}
