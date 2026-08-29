package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AirbendAllOtherCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AirbendAllOtherCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AirbendSupport airbendSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AirbendAllOtherCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Set<UUID> excludedIds = new HashSet<>(entry.targetsForEffect(effect));
        if (excludedIds.isEmpty() && entry.getTargetId() != null) {
            excludedIds.add(entry.getTargetId());
        }

        List<Permanent> creatures = gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .filter(permanent -> !excludedIds.contains(permanent.getId()))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();

        for (Permanent creature : creatures) {
            airbendSupport.airbend(gameData, entry, creature);
        }
        triggerCollectionService.checkBendingTriggers(gameData, entry.getControllerId(), BendingType.AIRBEND);
    }
}
