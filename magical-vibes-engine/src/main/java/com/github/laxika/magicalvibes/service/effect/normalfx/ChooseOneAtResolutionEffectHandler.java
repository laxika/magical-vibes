package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a modal whose choice is intentionally deferred until the ability resolves. */
@Component
@RequiredArgsConstructor
public class ChooseOneAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOneAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choice = ((ChooseOneAtResolutionEffect) effect).choice();
        playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                choice, false, entry.getSourcePermanentId());
    }
}
