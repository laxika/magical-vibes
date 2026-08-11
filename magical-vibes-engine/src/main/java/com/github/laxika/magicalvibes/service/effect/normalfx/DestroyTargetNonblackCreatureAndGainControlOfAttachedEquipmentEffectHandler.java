package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetNonblackCreatureAndGainControlOfAttachedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Murderous Spoils by preserving the target's attached Equipment before destruction. */
@Component
@RequiredArgsConstructor
public class DestroyTargetNonblackCreatureAndGainControlOfAttachedEquipmentEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetNonblackCreatureAndGainControlOfAttachedEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        List<Permanent> attachedEquipment = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (target.getId().equals(permanent.getAttachedTo())
                    && GameQueryService.permanentHasSubtype(permanent, CardSubtype.EQUIPMENT)) {
                attachedEquipment.add(permanent);
            }
        });

        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), true);

        GainControlOfTargetEffect gainControl = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (Permanent equipment : attachedEquipment) {
            UUID equipmentController = gameQueryService.findPermanentController(gameData, equipment.getId());
            if (equipmentController != null && !equipmentController.equals(entry.getControllerId())) {
                creatureControlService.applyControlEffect(gameData, entry.getControllerId(), equipment,
                        gainControl, ControlDuration.PERMANENT.toEffectDuration(), null,
                        entry.getCard().getName());
            }
        }
    }
}
