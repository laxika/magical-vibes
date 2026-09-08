package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an odd/even choice that belongs to the current spell or ability resolution. */
@Component
@RequiredArgsConstructor
public class ChooseManaValueParityAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseManaValueParityAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellManaValueParity != null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginSpellManaValueParityChoice(gameData, entry.getControllerId());
    }
}
