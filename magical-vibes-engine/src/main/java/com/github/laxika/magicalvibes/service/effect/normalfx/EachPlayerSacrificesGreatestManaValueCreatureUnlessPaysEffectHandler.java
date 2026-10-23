package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Tariff: each player sacrifices the creature they control with the greatest mana value
 * unless they pay that creature's mana cost. Delegates the per-player sequence to {@link TariffSupport}.
 * It only reads (or opens a prompt) before the simultaneous sacrifice, so it
 * {@link AsksDeathReplacementsFirst}.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst {

    private final TariffSupport tariffSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        tariffSupport.begin(gameData, entry.getCard());
    }
}
