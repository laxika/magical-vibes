package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberAtResolutionEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a number choice that belongs to the current spell or ability resolution. */
@Component
@RequiredArgsConstructor
public class ChooseNumberAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNumberAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellNumber != null || gameData.chosenXValue != null) {
            entry.setXValue(gameData.chosenSpellNumber != null
                    ? gameData.chosenSpellNumber : gameData.chosenXValue);
            gameData.chosenSpellNumber = null;
            gameData.chosenXValue = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        ChooseNumberAtResolutionEffect chooseEffect = (ChooseNumberAtResolutionEffect) effect;
        int minNumber = chooseEffect.minNumber();
        int maxNumber = chooseEffect.maxNumber();
        if (maxNumber == Integer.MAX_VALUE) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    entry.getControllerId(), minNumber, maxNumber, "Choose a number.", entry.getCard().getName()));
            return;
        }
        playerInputService.beginSpellNumberChoice(
                gameData, entry.getControllerId(), minNumber, maxNumber);
    }
}
