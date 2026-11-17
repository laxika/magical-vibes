package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetMaximumLifeTotalEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a persistent maximum life total for the spell's controller. */
@Component
@RequiredArgsConstructor
public class SetMaximumLifeTotalEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetMaximumLifeTotalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getControllerId() == null) {
            return;
        }

        SetMaximumLifeTotalEffect maximumLifeTotalEffect = (SetMaximumLifeTotalEffect) effect;
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, entry.getControllerId(), Duration.PERMANENT));

        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        gameLogService.append(gameData, GameLog.text(
                playerName + "'s maximum life total becomes "
                        + maximumLifeTotalEffect.maximumLifeTotal() + "."));
    }
}
