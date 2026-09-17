package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAllCountersFromTargetPermanentToTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a two-target move of all counters of one type, retaining the moved count as event value. */
@Component
@RequiredArgsConstructor
public class MoveAllCountersFromTargetPermanentToTargetPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveAllCountersFromTargetPermanentToTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            entry.setEventValue(0);
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, targets.get(0));
        Permanent destination = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (source == null || destination == null) {
            entry.setEventValue(0);
            return;
        }

        var move = (MoveAllCountersFromTargetPermanentToTargetPermanentEffect) effect;
        int available = source.getCounterCount(move.counterType());
        if (available <= 0) {
            entry.setEventValue(0);
            return;
        }

        int moved = permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, destination, move.counterType(), available);
        if (moved > 0) {
            permanentCounterSupport.removeCounterFromPermanent(
                    gameData, source, move.counterType(), moved);
        }
        entry.setEventValue(moved);
    }
}
