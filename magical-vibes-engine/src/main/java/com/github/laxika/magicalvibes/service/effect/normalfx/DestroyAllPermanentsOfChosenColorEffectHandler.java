package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsOfChosenColorEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Opens the resolution-time color choice for a chosen-color mass destruction effect.
 */
@Component
@RequiredArgsConstructor
public class DestroyAllPermanentsOfChosenColorEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllPermanentsOfChosenColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroyEffect = (DestroyAllPermanentsOfChosenColorEffect) effect;
        playerInputService.beginDestroyAllPermanentsOfChosenColorChoice(
                gameData, entry.getControllerId(), destroyEffect.filter());
    }
}
