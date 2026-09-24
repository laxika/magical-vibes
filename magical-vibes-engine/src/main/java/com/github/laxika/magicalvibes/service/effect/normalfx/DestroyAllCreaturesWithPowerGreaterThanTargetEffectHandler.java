package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesWithPowerGreaterThanTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Fell the Mighty by deriving the wipe threshold from its target. */
@Component
@RequiredArgsConstructor
public class DestroyAllCreaturesWithPowerGreaterThanTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestroyAllPermanentsEffectHandler destroyAllPermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllCreaturesWithPowerGreaterThanTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        UUID targetId = targets.isEmpty() ? entry.getTargetId() : targets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        int minimumPower = gameQueryService.getEffectivePower(gameData, target) + 1;
        destroyAllPermanentsEffectHandler.resolve(gameData, entry, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(minimumPower)))));
    }
}
