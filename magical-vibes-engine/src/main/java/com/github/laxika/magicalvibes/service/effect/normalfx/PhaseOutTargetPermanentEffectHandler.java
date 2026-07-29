package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves {@link PhaseOutTargetPermanentEffect}: phases out the targeted permanent. If the target
 * already left the battlefield the spell simply does nothing.
 */
@Component
@RequiredArgsConstructor
public class PhaseOutTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        phasingService.phaseOut(gameData, List.of(target));
    }
}
