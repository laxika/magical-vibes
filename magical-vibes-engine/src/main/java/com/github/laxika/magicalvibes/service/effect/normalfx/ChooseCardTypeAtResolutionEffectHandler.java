package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Asks for a card type and resolves again with the answer, which it keeps on the entry. */
@Component
@RequiredArgsConstructor
public class ChooseCardTypeAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardTypeAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellCardType == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCardTypeChoice(gameData, entry.getControllerId());
            return;
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        entry.setChosenCardType(gameData.chosenSpellCardType);
        gameData.chosenSpellCardType = null;
        gameData.chosenSpellPermanentType = null;
    }
}
