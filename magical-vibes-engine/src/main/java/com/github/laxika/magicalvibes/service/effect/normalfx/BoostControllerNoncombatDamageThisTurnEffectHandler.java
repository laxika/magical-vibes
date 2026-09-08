package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostControllerNoncombatDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostControllerNoncombatDamageThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostControllerNoncombatDamageThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int bonus = Math.max(0, entry.getXValue());
        gameData.controllerNoncombatDamageBonusThisTurn.merge(entry.getControllerId(), bonus, Integer::sum);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": noncombat damage dealt by sources you control gets +" + bonus + " this turn."));
    }
}
