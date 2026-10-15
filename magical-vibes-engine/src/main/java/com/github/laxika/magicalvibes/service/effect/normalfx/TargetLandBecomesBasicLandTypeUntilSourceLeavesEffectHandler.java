package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a source-bound basic land type change for a target land. */
@Component
@RequiredArgsConstructor
public class TargetLandBecomesBasicLandTypeUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        // A floating layer-4 effect stamped now (CR 613.7), so it applies after every type change
        // that already existed when the ability resolved, and ends when the source leaves.
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), source.getCard().getName(), source.getId(), entry.getControllerId(),
                new GrantSubtypeEffect(e.subtype(), GrantScope.TARGET, true),
                target.getId(), null, null, EffectDuration.WHILE_SOURCE_REMAINS, 0));
        gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(), " becomes a "
                + e.subtype().getDisplayName() + " until ", source.getCard(), " leaves the battlefield."));
    }
}
