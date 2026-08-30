package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Creates a dynamic basic-subtype grant tied to a counter on the targeted permanent. */
@Component
@RequiredArgsConstructor
public class GrantSubtypeToTargetWhileHasCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeToTargetWhileHasCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantSubtypeToTargetWhileHasCounterEffect) effect;
        for (UUID targetId : targetIds(entry, effect)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            PermanentPredicate scope = new PermanentAllOfPredicate(List.of(
                    new PermanentIsSpecificPermanentPredicate(targetId),
                    new PermanentIsLandPredicate(),
                    new PermanentHasCountersPredicate(grant.counterType())));
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new GrantSubtypeEffect(grant.subtype(), GrantScope.ALL_PERMANENTS, false, scope),
                    null, null, scope, EffectDuration.PERMANENT, 0));
        }
    }

    private static List<UUID> targetIds(StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        if (!targets.isEmpty()) {
            return targets;
        }
        return entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
    }
}
