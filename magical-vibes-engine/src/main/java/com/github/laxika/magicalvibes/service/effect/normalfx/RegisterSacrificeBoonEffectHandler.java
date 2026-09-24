package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.SacrificeBoonWatcher;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterSacrificeBoonEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a finite digital boon that watches the controller's sacrifices. */
@Component
@RequiredArgsConstructor
public class RegisterSacrificeBoonEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterSacrificeBoonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterSacrificeBoonEffect boon = (RegisterSacrificeBoonEffect) effect;
        if (boon.uses() <= 0) {
            return;
        }
        gameData.sacrificeBoonWatchers.add(new SacrificeBoonWatcher(
                entry.getControllerId(), entry.getCard(), boon.triggeredEffect(), boon.uses()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " gives its controller a sacrifice boon."));
    }
}
