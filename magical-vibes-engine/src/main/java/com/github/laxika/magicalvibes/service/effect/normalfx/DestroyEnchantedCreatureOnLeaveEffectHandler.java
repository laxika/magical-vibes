package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the captured destruction of an Aura's former enchanted creature. */
@Component
@RequiredArgsConstructor
public class DestroyEnchantedCreatureOnLeaveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEnchantedCreatureOnLeaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyEnchantedCreatureOnLeaveEffect e = (DestroyEnchantedCreatureOnLeaveEffect) effect;
        if (e.enchantedPermanentId() == null) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, e.enchantedPermanentId());
        if (enchantedCreature == null) {
            return;
        }

        destructionSupport.tryDestroyAndLog(
                gameData, enchantedCreature, entry.getCard().getName(), e.cannotBeRegenerated());
    }
}
