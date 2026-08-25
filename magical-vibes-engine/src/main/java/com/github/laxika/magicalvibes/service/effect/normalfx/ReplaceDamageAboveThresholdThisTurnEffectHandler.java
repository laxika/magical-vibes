package com.github.laxika.magicalvibes.service.effect.normalfx;

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
        gameData.damageReplacementsThisTurn.add(replacement);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": damage of at least " + replacement.threshold() + " is replaced with "
                        + replacement.replacementDamage() + " this turn."));
    }
}
