package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAllCardsOfChosenColorEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DiscardAllCardsOfChosenColorEffect} (Persecute): pauses to let the controller
 * choose a color; the actual reveal-and-discard runs in
 * {@code ChoiceHandlerService.handleDiscardChosenColorChoice} once the color is picked.
 */
@Component
@RequiredArgsConstructor
public class DiscardAllCardsOfChosenColorEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardAllCardsOfChosenColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInputService.beginDiscardChosenColorChoice(gameData, entry.getControllerId(), entry.getTargetId());
    }
}
