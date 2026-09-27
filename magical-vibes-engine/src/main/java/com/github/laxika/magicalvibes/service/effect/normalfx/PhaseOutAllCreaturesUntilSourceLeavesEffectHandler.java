package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutAllCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Out of Time's mass source-linked phase-out. */
@Component
@RequiredArgsConstructor
public class PhaseOutAllCreaturesUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutAllCreaturesUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<Permanent> creatures = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        });

        int phasedOutCount = 0;
        for (Permanent creature : creatures) {
            phasingService.phaseOutUntilSourceLeaves(gameData, source, creature);
            if (isHeldBySource(gameData, source.getId(), creature.getId())) {
                phasedOutCount++;
            }
        }

        if (phasedOutCount > 0) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, source, CounterType.TIME, phasedOutCount);
            entry.setSourcePermanentSnapshot(new Permanent(source));
        }
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private boolean isHeldBySource(GameData gameData, UUID sourceId, UUID targetId) {
        return gameData.phasedOutUntilSourceLeaves.getOrDefault(sourceId, java.util.Set.of())
                .contains(targetId);
    }
}
