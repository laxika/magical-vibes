package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves targeted airbend effects. */
@Component
@RequiredArgsConstructor
public class AirbendTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AirbendSupport airbendSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AirbendTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = new ArrayList<>(entry.targetsForEffect(effect));
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                gameLogService.append(gameData,
                        GameLog.cardThen(entry.getCard(), " fizzles (target no longer on the battlefield)."));
                continue;
            }

            airbendSupport.airbend(gameData, entry, target);
        }
        triggerCollectionService.checkBendingTriggers(gameData, entry.getControllerId(), BendingType.AIRBEND);
    }
}
