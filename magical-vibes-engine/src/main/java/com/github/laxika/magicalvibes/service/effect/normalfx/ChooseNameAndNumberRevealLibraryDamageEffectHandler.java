package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNameAndNumberRevealLibraryDamageEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChooseNameAndNumberRevealLibraryDamageEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNameAndNumberRevealLibraryDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseNameAndNumberRevealLibraryDamageEffect) effect;

        playerInputService.beginRevealLibraryNameGuessChoice(
                gameData, entry.getControllerId(), entry.getTargetId(), e.excludedTypes(), e.damage(), entry.getCard());
    }
}
