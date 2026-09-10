package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentsToTargetCreatureThenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentsToTargetCreatureThenEffectHandler implements NormalEffectHandlerBean {

    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentsToTargetCreatureThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var attachEffect = (AttachTargetEquipmentsToTargetCreatureThenEffect) effect;
        List<UUID> creatureTargets = entry.targetsForGroup(attachEffect.creatureTargetGroup());
        if (creatureTargets.isEmpty()) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, creatureTargets.getFirst());
        if (creature == null
                || !gameQueryService.isCreature(gameData, creature)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, creature.getId()))) {
            return;
        }

        int attachments = 0;
        for (UUID equipmentId : entry.targetsForGroup(attachEffect.equipmentTargetGroup())) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
            if (equipment == null
                    || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, equipment.getId()))
                    || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                    || equipment.getId().equals(creature.getId())
                    || creature.getId().equals(equipment.getAttachedTo())) {
                continue;
            }
            if (equipSupport.attachEquipment(gameData, equipment, creature)) {
                attachments++;
                gameLogService.append(gameData,
                        GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
            }
        }

        if (attachments == 0) {
            return;
        }

        StackEntry reflexiveContext = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                new ArrayList<>(),
                0,
                creature.getId());
        reflexiveContext.setSourcePermanentSnapshot(new Permanent(creature));
        queueReflexiveAbilityEffectHandler.resolve(
                gameData,
                reflexiveContext,
                new QueueReflexiveAbilityEffect(attachEffect.thenEffect(), true));
    }
}
