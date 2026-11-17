package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplaceDamageAboveThresholdThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReplaceDamageAboveThresholdThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReplaceDamageAboveThresholdThisTurnEffect replacement =
                (ReplaceDamageAboveThresholdThisTurnEffect) effect;
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), replacement, null, Duration.UNTIL_END_OF_TURN));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": damage of at least " + replacement.threshold() + " is replaced with "
                        + replacement.replacementDamage() + " this turn."));
    }
}
