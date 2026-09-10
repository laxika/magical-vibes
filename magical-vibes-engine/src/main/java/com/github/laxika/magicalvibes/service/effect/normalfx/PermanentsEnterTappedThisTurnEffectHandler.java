package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsEnterTappedThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermanentsEnterTappedThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PermanentsEnterTappedThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PermanentsEnterTappedThisTurnEffect) effect;
        if (e.filter() == null) {
            gameData.allPermanentsEnterTappedThisTurn = true;
        } else {
            gameData.permanentEnterTappedFiltersThisTurn
                    .computeIfAbsent(entry.getControllerId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(e.filter());
        }

        String logEntry = e.filter() == null
                ? "Permanents enter tapped this turn."
                : "Matching permanents you control enter tapped this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
    }
}
