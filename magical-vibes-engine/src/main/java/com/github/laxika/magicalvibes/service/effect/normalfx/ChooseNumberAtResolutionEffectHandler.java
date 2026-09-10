package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberAtResolutionEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a number choice that belongs to the current spell or ability resolution. */
@Component
@RequiredArgsConstructor
public class ChooseNumberAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNumberAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellNumber != null) {
            entry.setXValue(gameData.chosenSpellNumber);
            gameData.chosenSpellNumber = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginSpellNumberChoice(
                gameData, entry.getControllerId(), ((ChooseNumberAtResolutionEffect) effect).maxNumber());
    }
}
