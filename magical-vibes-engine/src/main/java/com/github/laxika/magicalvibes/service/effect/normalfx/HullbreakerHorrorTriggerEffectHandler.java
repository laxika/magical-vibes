package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HullbreakerHorrorTriggerEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Hullbreaker Horror's spell-cast modal trigger. Mode is picked as the ability resolves
 * ({@code ChoiceContext.HullbreakerHorrorModeChoice}); the chosen mode's targeted bounce then routes
 * through {@code MayAbilityTriggerTarget}.
 */
@Component
@RequiredArgsConstructor
public class HullbreakerHorrorTriggerEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HullbreakerHorrorTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInputService.beginHullbreakerHorrorModeChoice(gameData, entry.getControllerId(), entry.getCard());
    }
}
