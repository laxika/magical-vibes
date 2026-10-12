package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves an {@link ExileSpellEffect} marked {@code immediately}: the resolving spell is exiled at this
 * point of its resolution. An unmarked effect is a disposition marker applied by the stack resolution
 * once the spell has finished resolving, so it does nothing here. A copy simply ceases to exist.
 */
@Component
@RequiredArgsConstructor
public class ExileSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (!((ExileSpellEffect) effect).exileImmediately() || entry.isCopy() || entry.isSpellMovedDuringResolution()) {
            return;
        }
        gameData.addToExile(entry.getOwnerId(), entry.getPhysicalCard());
        entry.setSpellMovedDuringResolution(true);
        gameLogService.append(gameData, GameLog.isExiled(entry.getCard()));
    }
}
