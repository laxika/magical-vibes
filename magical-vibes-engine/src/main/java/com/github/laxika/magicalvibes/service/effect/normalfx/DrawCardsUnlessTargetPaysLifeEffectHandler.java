package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsUnlessTargetPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import org.springframework.stereotype.Component;

/** Resolves a targeted opponent's choice to pay life instead of letting the controller draw. */
@Component
public class DrawCardsUnlessTargetPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final MayPayLifeEffectResolutionHandler mayPayLifeEffectResolutionHandler;

    public DrawCardsUnlessTargetPaysLifeEffectHandler(
            MayPayLifeEffectResolutionHandler mayPayLifeEffectResolutionHandler) {
        this.mayPayLifeEffectResolutionHandler = mayPayLifeEffectResolutionHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardsUnlessTargetPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardsUnlessTargetPaysLifeEffect drawEffect = (DrawCardsUnlessTargetPaysLifeEffect) effect;
        mayPayLifeEffectResolutionHandler.resolve(gameData, entry, new MayPayLifeEffect(
                drawEffect.lifeCost(),
                new DrawCardEffect(0),
                "Pay " + drawEffect.lifeCost() + " life to prevent drawing?",
                MayPayPayer.TRIGGERING_PLAYER,
                new DrawCardEffect(drawEffect.drawCount())));
    }
}
