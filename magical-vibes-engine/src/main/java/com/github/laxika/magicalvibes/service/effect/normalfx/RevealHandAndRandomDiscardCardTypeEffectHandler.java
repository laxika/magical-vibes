package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndRandomDiscardCardTypeEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealHandAndRandomDiscardCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandAndRandomDiscardCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealHandAndRandomDiscardCardTypeEffect) effect;
        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveRevealHandAndRandomDiscardOfType(
                gameData, entry.getTargetId(), entry.getCard().getName(), e.cardType());
    }
}
