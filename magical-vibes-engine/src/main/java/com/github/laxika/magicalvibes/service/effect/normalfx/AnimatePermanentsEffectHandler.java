package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AnimatePermanentsEffect} by dispatching on its duration and scope to the matching
 * {@link AnimationSupport} branch. Each branch preserves the behaviour of the record it replaced
 * (until-end-of-turn self/target, until-your-next-turn own-lands, controlled-mass, permanent-target,
 * and while-source-on-battlefield target animations).
 */
@Component
@RequiredArgsConstructor
public class AnimatePermanentsEffectHandler implements NormalEffectHandlerBean {

    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimatePermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnimatePermanentsEffect) effect;
        switch (e.duration()) {
            case PERMANENT -> animationSupport.animatePermanentTarget(gameData, entry, e);
            case WHILE_SOURCE_ON_BATTLEFIELD -> animationSupport.animateWhileSource(gameData, entry, e);
            default -> {
                switch (e.scope()) {
                    case OWN_LANDS -> animationSupport.animateOwnLands(gameData, entry, e);
                    case ALL_LANDS -> animationSupport.animateAllLands(gameData, entry, e);
                    case OWN_PERMANENTS -> animationSupport.animateControlledPermanents(gameData, entry, e);
                    default -> animationSupport.animateSingle(gameData, entry, e);
                }
            }
        }
    }
}
