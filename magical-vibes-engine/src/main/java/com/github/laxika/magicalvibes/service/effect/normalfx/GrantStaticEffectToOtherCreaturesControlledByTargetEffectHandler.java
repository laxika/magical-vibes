package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BlockingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOtherCreaturesControlledByTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantStaticEffectToOtherCreaturesControlledByTargetEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToOtherCreaturesControlledByTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToOtherCreaturesControlledByTargetEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (targetControllerId == null) {
                continue;
            }

            var battlefield = gameData.playerBattlefields.get(targetControllerId);
            if (battlefield == null) {
                continue;
            }

            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (permanent.getId().equals(targetId) || !gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }

                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                        grant.staticEffect() instanceof BlockingRestrictionEffect
                                ? new GrantEffectEffect(grant.staticEffect(), GrantScope.SELF)
                                : grant.staticEffect(),
                        permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
            }
        }
    }
}
