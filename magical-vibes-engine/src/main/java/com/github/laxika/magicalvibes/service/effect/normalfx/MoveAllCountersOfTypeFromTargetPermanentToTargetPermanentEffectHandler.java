package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a targeted transfer of every counter of one kind and records its amount as event value. */
@Component
@RequiredArgsConstructor
public class MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.size() < 2) {
            entry.setEventValue(0);
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        Permanent destination = gameQueryService.findPermanentById(gameData, targetIds.get(1));
        MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect transfer =
                (MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect) effect;
        if (source == null || destination == null || source == destination) {
            entry.setEventValue(0);
            return;
        }

        int count = source.getCounterCount(transfer.counterType());
        entry.setEventValue(count);
        if (count <= 0) {
            return;
        }

        source.setCounterCount(transfer.counterType(), 0);
        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, destination, transfer.counterType(), count);
    }
}
