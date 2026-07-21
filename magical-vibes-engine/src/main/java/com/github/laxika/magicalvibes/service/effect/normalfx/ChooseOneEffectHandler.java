package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a modal ("choose one") triggered ability. Spells and enter-the-battlefield modals pick
 * their mode at cast time and never reach the stack as a raw {@link ChooseOneEffect}; a triggered
 * ability has no cast, so the engine picks the mode as the ability resolves (a list pick driven by
 * {@code ChoiceContext.ChooseModeChoice}). The chosen mode's effects are then spliced into this
 * ability's paused resolution and resolved in order — functionally equivalent to choosing the mode
 * as the ability is put on the stack for modes whose effects have no targets (Etherwrought Page).
 */
@Component
@RequiredArgsConstructor
public class ChooseOneEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                (ChooseOneEffect) effect);
    }
}
