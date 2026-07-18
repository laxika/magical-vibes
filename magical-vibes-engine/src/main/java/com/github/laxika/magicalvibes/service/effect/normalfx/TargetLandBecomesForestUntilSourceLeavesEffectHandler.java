package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandBecomesForestUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetLandBecomesForestUntilSourceLeavesEffect}: records the target land's id on
 * the source permanent's {@code forestedLandIds}. The companion static effect
 * ({@code TrackedLandsBecomeForestEffect}, layer 4) then continuously makes that land a Forest for
 * as long as the source stays on the battlefield.
 */
@Component
@RequiredArgsConstructor
public class TargetLandBecomesForestUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetLandBecomesForestUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }
        source.getForestedLandIds().add(target.getId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                target.getCard().getName() + " becomes a Forest until "
                        + source.getCard().getName() + " leaves the battlefield."));
    }
}
