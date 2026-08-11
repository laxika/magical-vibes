package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorThenDiscardFromTargetHandEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Begins the resolution-time color choice for a color-filtered hand discard.
 */
@Component
@RequiredArgsConstructor
public class ChooseColorThenDiscardFromTargetHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseColorThenDiscardFromTargetHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInputService.beginChooseColorThenDiscardFromTargetHandChoice(
                gameData, entry.getControllerId(), entry.getTargetId());
    }
}
