package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostControllerDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Floats a {@link BoostControllerDamageThisTurnEffect} on the controller until end of turn, its bonus fixed as it
 * resolves (CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class BoostControllerDamageThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostControllerDamageThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostControllerDamageThisTurnEffect) effect;
        int bonus = Math.max(0, amountEvaluationService.evaluate(gameData, boost.amount(),
                AmountContext.forStackEntry(entry, null)));
        var fixed = new BoostControllerDamageThisTurnEffect(new Fixed(bonus), boost.color(), boost.noncombatOnly());
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), fixed, entry.getControllerId(),
                Duration.UNTIL_END_OF_TURN));
        String damage = boost.noncombatOnly() ? "noncombat damage" : "damage";
        String sources = boost.color() == null ? "sources" : boost.color().name().toLowerCase() + " sources";
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": " + damage + " dealt by " + sources + " you control gets +" + bonus + " this turn."));
    }
}
