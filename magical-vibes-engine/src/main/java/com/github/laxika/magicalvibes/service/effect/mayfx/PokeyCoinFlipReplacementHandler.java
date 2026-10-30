package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PokeyTheScallywaggEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies the answer to Pokey, the Scallywagg's optional replacement of a coin flip with a d20 roll. */
@Component
@RequiredArgsConstructor
public class PokeyCoinFlipReplacementHandler implements MayEffectHandlerBean {

    private final CoinFlipService coinFlipService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PokeyTheScallywaggEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        coinFlipService.answerPokeyReplacement(gameData, accepted);
    }
}
