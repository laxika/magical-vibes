package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextSchemeSetInMotionReplacementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Registers Plots That Span Centuries' next-scheme replacement. */
@Component
@RequiredArgsConstructor
public class RegisterNextSchemeSetInMotionReplacementEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterNextSchemeSetInMotionReplacementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        gameData.pendingNextSchemeSetInMotionReplacements.merge(controllerId, 1, Integer::sum);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " waits for the next scheme you set in motion, which will set three schemes instead."));
    }
}
