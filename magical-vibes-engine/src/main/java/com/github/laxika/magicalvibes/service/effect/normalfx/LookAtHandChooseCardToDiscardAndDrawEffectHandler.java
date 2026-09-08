package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandChooseCardToDiscardAndDrawEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Oildeep Gearhulk's optional hand choice and draw rider.
 */
@Component
@RequiredArgsConstructor
public class LookAtHandChooseCardToDiscardAndDrawEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtHandChooseCardToDiscardAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInteractionSupport.resolveLookAtHandChooseCardToDiscardAndDraw(gameData, entry);
    }
}
