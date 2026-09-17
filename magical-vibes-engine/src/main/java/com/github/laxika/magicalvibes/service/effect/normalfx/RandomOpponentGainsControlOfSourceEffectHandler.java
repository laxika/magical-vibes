package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RandomOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the randomly selected opponent gaining control of the source permanent. */
@Component
@RequiredArgsConstructor
public class RandomOpponentGainsControlOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RandomOpponentGainsControlOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        UUID newControllerId = entry.getTargetId();
        if (newControllerId == null || !gameData.playerIds.contains(newControllerId)) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, newControllerId, source,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());
    }
}
