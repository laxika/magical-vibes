package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllPermanentsOfColorToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a fixed-color mass return-to-hand effect. */
@Component
@RequiredArgsConstructor
public class ReturnAllPermanentsOfColorToHandEffectHandler implements NormalEffectHandlerBean {

    private final BounceSupport bounceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllPermanentsOfColorToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnAllPermanentsOfColorToHandEffect) effect;
        bounceSupport.applyReturnAllPermanentsOfColorToHand(
                gameData, entry, returnEffect.color(), returnEffect.filter(), returnEffect.opponentsOnly());
    }
}
