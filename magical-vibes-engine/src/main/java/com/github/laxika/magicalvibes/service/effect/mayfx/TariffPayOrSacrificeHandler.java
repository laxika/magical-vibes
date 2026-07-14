package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TariffSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Tariff: pay this creature's mana cost or sacrifice it (e.g. Tariff, Smokestack-style demands).
 */
@Component
@RequiredArgsConstructor
public class TariffPayOrSacrificeHandler implements MayEffectHandlerBean {

    private final TariffSupport tariffSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        tariffSupport.handlePayOrSacrificeChoice(gameData, player, accepted, ability);
    }
}
