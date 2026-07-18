package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RelicBindTapEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Relic Bind's enchanted-artifact-tap triggered ability. The engine has no cast-time modal
 * machinery for triggered abilities, so the "choose one" mode is picked as the ability resolves (a
 * list pick driven by {@code ChoiceContext.RelicBindModeChoice}); the chosen mode's targeted effect
 * — 1 damage to a player/planeswalker, or a player gains 1 life — then routes through the shared
 * {@code MayAbilityTriggerTarget} target-selection flow. Both modes' targets are free, so this is
 * functionally equivalent to choosing mode and target as the ability is put on the stack.
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
