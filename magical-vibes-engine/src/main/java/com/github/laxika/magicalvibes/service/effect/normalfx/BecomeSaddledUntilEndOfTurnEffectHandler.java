package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BecomeSaddledUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeSaddledUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var saddle = (BecomeSaddledUntilEndOfTurnEffect) effect;
        List<UUID> targetIds;
        if (saddle.scope() == GrantScope.SELF) {
            UUID sourceId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getTargetId();
            targetIds = sourceId == null ? List.of() : List.of(sourceId);
        } else if (saddle.scope() == GrantScope.TARGET) {
            targetIds = entry.targetsForEffect(effect);
            if (targetIds.isEmpty() && entry.getTargetId() != null) {
                targetIds = List.of(entry.getTargetId());
            }
        } else {
            return;
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            boolean becameSaddled = !target.isSaddled();
            target.setSaddled(true);
            if (becameSaddled) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
                if (controllerId != null) {
                    triggerCollectionService.checkBecomesSaddledTriggers(gameData, target, controllerId);
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" becomes saddled until end of turn.").build());
        }
    }
}
