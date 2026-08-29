package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreaturesThenPutCountersOnSourceIfOwnDestroyedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetCreaturesThenPutCountersOnSourceIfOwnDestroyedEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreaturesThenPutCountersOnSourceIfOwnDestroyedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        List<Permanent> toDestroy = new ArrayList<>();
        Map<UUID, UUID> controllerByPermanentId = new HashMap<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            toDestroy.add(target);
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                controllerByPermanentId.put(targetId, controllerId);
            }
        }

        List<Permanent> destroyed = destructionSupport.destroyBatchCollecting(
                gameData, toDestroy, entry.getCard().getName(), false);
        boolean ownCreatureDestroyed = destroyed.stream()
                .map(Permanent::getId)
                .map(controllerByPermanentId::get)
                .anyMatch(entry.getControllerId()::equals);
        if (!ownCreatureDestroyed || entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, source, CounterType.PLUS_ONE_PLUS_ONE, 2);
        }
    }
}
