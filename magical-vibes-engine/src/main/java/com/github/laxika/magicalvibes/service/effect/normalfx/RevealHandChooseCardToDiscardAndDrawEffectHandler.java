package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandChooseCardToDiscardAndDrawEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a public hand reveal with an optional filtered discard and draw.
 */
@Component
@RequiredArgsConstructor
public class RevealHandChooseCardToDiscardAndDrawEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandChooseCardToDiscardAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var revealEffect = (RevealHandChooseCardToDiscardAndDrawEffect) effect;
        playerInteractionSupport.resolveRevealHandChooseCardToDiscardAndDraw(
                gameData, entry, revealEffect.filter());
    }
}
