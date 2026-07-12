package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyEnchantedPermanentEffect} (Spreading Algae): destroys the permanent the
 * source Aura is currently attached to. If the Aura is no longer attached (or the enchanted permanent
 * has already left), nothing happens.
 */
@Component
@RequiredArgsConstructor
public class DestroyEnchantedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEnchantedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted != null) {
            destructionSupport.tryDestroyAndLog(gameData, enchanted, entry.getCard().getName());
        }
    }
}
