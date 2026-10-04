package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBaseStatsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Installs stats and abilities on the affected creature while its counters remain. */
@Component
@RequiredArgsConstructor
public class GrantBaseStatsToCounterBearersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantBaseStatsToCounterBearersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var rule = (GrantBaseStatsToCounterBearersEffect) effect;
        Permanent target = entry.getTargetId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || target.getCounterCount(rule.counterType()) <= 0
                || gameQueryService.cantHaveCounters(gameData, target)) return;
        var scope = new PermanentAllOfPredicate(List.of(
                new PermanentIsSpecificPermanentPredicate(target.getId()),
                new PermanentHasCountersPredicate(rule.counterType(),
                        target.getLastCounterRemovalVersions().getOrDefault(rule.counterType(), 0L))));
        String sourceName = entry.getCard().getName();
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceName, null,
                entry.getControllerId(), new SetBasePowerToughnessEffect(rule.power(), rule.toughness(), GrantScope.ALL_CREATURES),
                null, null, scope, EffectDuration.PERMANENT, 0));
        if (!rule.keywords().isEmpty()) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceName, null,
                    entry.getControllerId(), new GrantKeywordEffect(rule.keywords(), GrantScope.ALL_CREATURES),
                    null, null, scope, EffectDuration.PERMANENT, 0));
        }
    }
}