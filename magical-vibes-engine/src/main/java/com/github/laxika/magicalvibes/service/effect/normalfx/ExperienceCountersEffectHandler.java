package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExperienceCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExperienceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExperienceCountersEffect experience = (ExperienceCountersEffect) effect;
        int updated = gameData.playerExperienceCounters.merge(
                entry.getControllerId(), experience.amount(), Integer::sum);
        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        gameLogService.append(gameData, GameLog.text(playerName + " gets " + experience.amount()
                + " experience counter(s) (" + updated + " total)."));
    }
}
