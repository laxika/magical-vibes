package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Tariff: each player sacrifices the creature they control with the greatest mana value
 * unless they pay that creature's mana cost. Delegates the per-player sequence to {@link TariffSupport}.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffectHandler implements NormalEffectHandlerBean {

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
