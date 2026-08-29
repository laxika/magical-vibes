package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfDamagedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Crag Saurian's source-controller control-change trigger. */
@Component
@RequiredArgsConstructor
public class DamageSourceControllerGainsControlOfDamagedPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageSourceControllerGainsControlOfDamagedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controlEffect = (DamageSourceControllerGainsControlOfDamagedPermanentEffect) effect;
        UUID newControllerId = controlEffect.damageSourceControllerId();
        UUID damagedPermanentId = entry.getSourcePermanentId();
        if (newControllerId == null || !gameData.playerIds.contains(newControllerId)
                || damagedPermanentId == null) {
            return;
        }

        Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, damagedPermanentId);
        if (damagedPermanent == null) return;

        creatureControlService.applyControlEffect(gameData, newControllerId, damagedPermanent,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                null, entry.getCard().getName());
    }
}
