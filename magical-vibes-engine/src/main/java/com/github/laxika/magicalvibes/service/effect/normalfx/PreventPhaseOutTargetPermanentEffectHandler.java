package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventPhaseOutTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventPhaseOutTargetPermanentEffect}: marks the targeted permanent as unable to
 * phase out until the resolving controller's next upkeep. If the target already left the
 * battlefield the ability simply does nothing.
 */
@Component
@RequiredArgsConstructor
public class PreventPhaseOutTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventPhaseOutTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setCantPhaseOutUntilUpkeepOf(entry.getControllerId());
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't phase out."));
    }
}
