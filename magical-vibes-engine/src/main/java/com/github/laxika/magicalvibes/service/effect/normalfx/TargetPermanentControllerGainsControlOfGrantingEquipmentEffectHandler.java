package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerGainsControlOfGrantingEquipmentEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves control of the Equipment that granted an activated ability. */
@Component
@RequiredArgsConstructor
public class TargetPermanentControllerGainsControlOfGrantingEquipmentEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPermanentControllerGainsControlOfGrantingEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPermanentControllerGainsControlOfGrantingEquipmentEffect) effect;
        if (e.sourceHadExcludedSubtype()) {
            return;
        }

        Permanent equipment = e.equipmentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, e.equipmentId());
        if (equipment == null || entry.getTargetId() == null) {
            return;
        }

        var newControllerId = gameQueryService.findPermanentController(gameData, entry.getTargetId());
        if (newControllerId == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, newControllerId, equipment,
                new GainControlOfTargetEffect(e.duration()),
                EffectDuration.PERMANENT, null, entry.getCard().getName());
    }
}
