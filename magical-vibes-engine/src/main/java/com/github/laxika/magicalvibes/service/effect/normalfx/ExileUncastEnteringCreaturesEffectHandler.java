package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUncastEnteringCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileUncastEnteringCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUncastEnteringCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (((ExileUncastEnteringCreaturesEffect) effect).nontokenOnly()) {
            gameData.playersExilingUncastEnteringNontokenCreaturesThisTurn.add(entry.getControllerId());
            gameLogService.append(gameData, GameLog.text(
                    "Nontoken creatures that would enter without having been cast are exiled instead this turn."));
            return;
        }
        gameData.playersExilingUncastEnteringCreaturesThisTurn.add(entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(
                "Creatures that would enter without having been cast are exiled instead this turn."));
    }
}
