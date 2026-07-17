package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerSacrificesCreatureOnLeaveEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Funeral March's leaves-the-battlefield trigger: the player who controlled the enchanted
 * creature when it left the battlefield sacrifices a creature of their choice. The controller was
 * baked into the effect at trigger time (before the creature was gone).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnchantedControllerSacrificesCreatureOnLeaveEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedControllerSacrificesCreatureOnLeaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedControllerSacrificesCreatureOnLeaveEffect) effect;
        if (e.enchantedControllerId() == null) {
            return;
        }
        destructionSupport.performSacrificeCreatureForPlayer(gameData, e.enchantedControllerId());
    }
}
