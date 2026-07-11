package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandChooseNonlandToBottomAndDrawEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Vendilion Clique's ETB: look at target player's hand, then the caster may choose a nonland card
 * to reveal, put on the bottom of that player's library, and make them draw a card. Delegates to
 * {@link PlayerInteractionSupport#resolveLookAtHandChooseNonlandToBottom}.
 */
@Component
@RequiredArgsConstructor
public class LookAtHandChooseNonlandToBottomAndDrawEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtHandChooseNonlandToBottomAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInteractionSupport.resolveLookAtHandChooseNonlandToBottom(gameData, entry);
    }
}
