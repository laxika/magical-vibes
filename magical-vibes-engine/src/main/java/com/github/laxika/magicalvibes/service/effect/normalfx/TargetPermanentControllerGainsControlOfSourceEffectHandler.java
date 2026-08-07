package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPermanentControllerGainsControlOfSourceEffect}: the controller of the
 * ability's targeted permanent gains control of the source permanent. Nothing happens if either the
 * source or the target has left the battlefield.
 */
@Component
@RequiredArgsConstructor
public class TargetPermanentControllerGainsControlOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPermanentControllerGainsControlOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPermanentControllerGainsControlOfSourceEffect) effect;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || entry.getTargetId() == null) {
            return;
        }

        UUID newControllerId = gameQueryService.findPermanentController(gameData, entry.getTargetId());
        if (newControllerId == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, newControllerId, source,
                new GainControlOfTargetEffect(e.duration()),
                EffectDuration.PERMANENT, null, entry.getCard().getName());
    }
}
