package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureAttachedToEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyCreatureAttachedToEnchantedEquipmentEffect} (Artificer's Hex): follows the
 * source Aura to the Equipment it enchants, then to the creature that Equipment is attached to, and
 * destroys that creature. Any broken link in that chain (Aura unattached, Equipment unattached, or the
 * host no longer a creature) makes the effect do nothing.
 */
@Component
@RequiredArgsConstructor
public class DestroyCreatureAttachedToEnchantedEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreatureAttachedToEnchantedEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            return;
        }
        Permanent equipment = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (equipment == null || !equipment.isAttached()) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        destructionSupport.tryDestroyAndLog(gameData, creature, entry.getCard().getName());
    }
}
