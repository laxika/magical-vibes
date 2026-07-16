package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerPermanentOfChosenColorEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CreateTokenPerPermanentOfChosenColorEffect} (Rith, the Awakener): pauses to let
 * the controller choose a color. The count-and-create-tokens step runs in
 * {@code ChoiceHandlerService.handleCreateTokensPerPermanentOfChosenColorChoice} once the color is
 * picked.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenPerPermanentOfChosenColorEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenPerPermanentOfChosenColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenPerPermanentOfChosenColorEffect) effect;
        playerInputService.beginCreateTokensPerPermanentOfChosenColorChoice(gameData, entry.getControllerId(),
                e.tokenTemplate(), entry.getCard().getSetCode());
    }
}
