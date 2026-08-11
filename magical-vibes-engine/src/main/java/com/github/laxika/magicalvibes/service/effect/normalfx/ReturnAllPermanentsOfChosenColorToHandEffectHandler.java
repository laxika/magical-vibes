package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllPermanentsOfChosenColorToHandEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ReturnAllPermanentsOfChosenColorToHandEffect} by opening its resolution-time
 * color choice.
 */
@Component
@RequiredArgsConstructor
public class ReturnAllPermanentsOfChosenColorToHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllPermanentsOfChosenColorToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnAllPermanentsOfChosenColorToHandEffect) effect;
        playerInputService.beginReturnAllPermanentsOfChosenColorChoice(gameData, entry.getControllerId(),
                returnEffect.filter());
    }
}
