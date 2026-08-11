package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SetCardTypesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetCardTypesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var setTypes = (SetCardTypesUntilEndOfTurnEffect) effect;
        List<UUID> targetIds;
        if (setTypes.scope() == GrantScope.SELF) {
            UUID sourceId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getTargetId();
            targetIds = sourceId == null ? List.of() : List.of(sourceId);
        } else if (setTypes.scope() == GrantScope.TARGET) {
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
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(), setTypes, target.getId(), null, null,
                    EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}
