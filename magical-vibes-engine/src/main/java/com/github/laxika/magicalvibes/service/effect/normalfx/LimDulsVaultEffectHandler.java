package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LimDulsVaultEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LimDulsVaultEffect}: performs the first mandatory look at the top five cards,
 * after which the loop is driven by the repeat and order interaction handlers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimDulsVaultEffectHandler implements NormalEffectHandlerBean {

    private final LimDulsVaultSupport limDulsVaultSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LimDulsVaultEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        limDulsVaultSupport.beginLook(gameData, entry.getControllerId());
    }
}
