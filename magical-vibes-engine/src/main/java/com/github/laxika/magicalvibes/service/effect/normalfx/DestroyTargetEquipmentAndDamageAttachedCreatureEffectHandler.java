package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetEquipmentAndDamageAttachedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Unforge while preserving the Equipment's attached creature through destruction. */
@Component
@RequiredArgsConstructor
public class DestroyTargetEquipmentAndDamageAttachedCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetEquipmentAndDamageAttachedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (equipment == null) {
            return;
        }

        Permanent attachedCreature = equipment.getAttachedTo() == null
                ? null
                : gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (attachedCreature != null && !gameQueryService.isCreature(gameData, attachedCreature)) {
            attachedCreature = null;
        }

        boolean destroyed = destructionSupport.tryDestroyAndLog(
                gameData, equipment, entry.getCard().getName());
        if (destroyed && attachedCreature != null) {
            damageSupport.dealCreatureDamage(gameData, entry, attachedCreature, 2);
        }
    }
}
