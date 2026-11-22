package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Asks for a creature type and resolves again with the answer, which it keeps on the entry. */
@Component
@RequiredArgsConstructor
public class ChooseCreatureTypeAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureTypeAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId());
            return;
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        entry.setChosenCreatureType(gameData.chosenSpellSubtype);
        gameData.chosenSpellSubtype = null;
    }
}
