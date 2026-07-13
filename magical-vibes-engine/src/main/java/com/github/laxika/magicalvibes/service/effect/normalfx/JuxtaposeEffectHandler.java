package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.JuxtaposeEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Juxtapose: the controller and the target player exchange control of their greatest
 * mana value creature, then their greatest mana value artifact. Delegates the sequence to
 * {@link JuxtaposeSupport}.
 */
@Component
@RequiredArgsConstructor
public class JuxtaposeEffectHandler implements NormalEffectHandlerBean {

    private final JuxtaposeSupport juxtaposeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return JuxtaposeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        juxtaposeSupport.begin(gameData, entry.getCard(), entry.getControllerId(), entry.getTargetId());
    }
}
