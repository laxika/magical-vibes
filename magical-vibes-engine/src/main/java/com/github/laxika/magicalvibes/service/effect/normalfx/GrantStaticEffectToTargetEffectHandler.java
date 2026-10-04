package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantStaticEffectToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantStaticEffectToTargetEffect grant = (GrantStaticEffectToTargetEffect) effect;

        List<UUID> ids = entry.targetsForEffect(effect);
        if (ids.isEmpty() && entry.getTargetId() != null) {
            ids = List.of(entry.getTargetId());
        }
        if (ids.isEmpty() && entry.getTargetIds() != null) {
            ids = entry.getTargetIds();
        }

        for (UUID targetId : ids) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            PermanentPredicate durationFilter = null;
            if (grant.whileCounterRemains() != null) {
                if (target.getCounterCount(grant.whileCounterRemains()) == 0) {
                    continue;
                }
                durationFilter = new PermanentHasCountersPredicate(grant.whileCounterRemains(),
                        target.getLastCounterRemovalVersions().getOrDefault(grant.whileCounterRemains(), 0L));
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    null,
                    entry.getControllerId(),
                    new GrantEffectEffect(grant.staticEffect(), GrantScope.TARGET, durationFilter),
                    target.getId(),
                    null,
                    durationFilter,
                    EffectDuration.PERMANENT,
                    0
            ));
        }
    }
}
