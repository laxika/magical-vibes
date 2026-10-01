package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPermanentUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a targeted phase-out held until the next planeswalk. */
@Component
@RequiredArgsConstructor
public class PhaseOutTargetPermanentUntilPlaneswalkEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutTargetPermanentUntilPlaneswalkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                phasingService.phaseOutUntilPlaneswalk(gameData, List.of(target));
            }
        }
    }
}
