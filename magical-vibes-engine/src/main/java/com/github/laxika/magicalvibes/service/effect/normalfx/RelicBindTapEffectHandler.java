package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RelicBindTapEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Compatibility resolver for an already queued legacy marker. New enchanted-permanent tap triggers
 * choose their mode and target through the shared triggered-modal queue before receiving priority,
 * and put the chosen damage or life-gain effect directly on the stack.
 */
@Component
@RequiredArgsConstructor
public class RelicBindTapEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RelicBindTapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInputService.beginRelicBindModeChoice(gameData, entry.getControllerId(), entry.getCard());
    }
}
