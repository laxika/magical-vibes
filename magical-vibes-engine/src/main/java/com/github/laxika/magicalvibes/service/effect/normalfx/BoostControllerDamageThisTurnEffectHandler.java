package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DamageBonusGrant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostControllerDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostControllerDamageThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostControllerDamageThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var bonus = (BoostControllerDamageThisTurnEffect) effect;
        GameData.addDamageBonusGrant(gameData.controllerDamageBonusThisTurn, entry.getControllerId(),
                new DamageBonusGrant(entry.getCard().getName(), null, bonus.amount()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": damage dealt by sources you control gets +" + bonus.amount() + " this turn."));
    }
}
