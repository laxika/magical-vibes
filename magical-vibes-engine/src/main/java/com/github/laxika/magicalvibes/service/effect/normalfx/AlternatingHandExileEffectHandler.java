package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AlternatingHandExileEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AlternatingHandExileEffect} (Struggle for Sanity) by revealing the targeted
 * player's hand and beginning the alternating exile picks; the piles are applied by
 * {@link com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService} once the hand is
 * empty.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlternatingHandExileEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AlternatingHandExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInteractionSupport.resolveAlternatingHandExile(gameData, entry);
    }
}
