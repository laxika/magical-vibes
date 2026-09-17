package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ComeuppanceShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ComeuppanceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Comeuppance's turn-scoped prevention and return-damage shield. */
@Component
@RequiredArgsConstructor
public class ComeuppanceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ComeuppanceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.comeuppanceShields.add(new ComeuppanceShield(controllerId, entry.getCard()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " prevents damage to " + gameData.playerIdToName.get(controllerId)
                        + " and planeswalkers they control this turn by sources they don't control."));
    }
}
