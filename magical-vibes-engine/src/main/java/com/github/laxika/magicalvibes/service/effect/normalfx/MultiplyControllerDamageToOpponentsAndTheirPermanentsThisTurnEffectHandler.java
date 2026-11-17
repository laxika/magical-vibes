package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var multiplication = (MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffect) effect;
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), multiplication, entry.getControllerId(),
                Duration.UNTIL_END_OF_TURN));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": damage dealt to opponents and their permanents by sources you control is multiplied by "
                        + multiplication.multiplier() + " this turn."));
    }
}
