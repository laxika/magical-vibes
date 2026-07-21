package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseTwoFilteredCardsFromTargetHandToDiscardEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Distended Mindbender: reveal target opponent's hand; choose one card matching each of two
 * predicates (when able); that player discards those cards.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseTwoFilteredCardsFromTargetHandToDiscardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseTwoFilteredCardsFromTargetHandToDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseTwoFilteredCardsFromTargetHandToDiscardEffect) effect;
        playerInteractionSupport.resolveHandRevealAndChooseTwoFilters(
                gameData, entry, e.firstFilter(), e.secondFilter());
    }
}
