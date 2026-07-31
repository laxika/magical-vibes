package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a modal ability whose modes all apply to the ability's single creature target: prompts
 * the controller for the mode through the same {@code ChooseModeChoice} flow
 * {@link ChooseOneEffectHandler} uses, then the chosen mode's effects are spliced into this
 * ability's paused resolution and read the target already on the stack entry.
 */
@Component
@RequiredArgsConstructor
public class ChooseOneForTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOneForTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var modal = (ChooseOneForTargetCreatureEffect) effect;
        playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                new ChooseOneEffect(modal.options()));
    }
}
