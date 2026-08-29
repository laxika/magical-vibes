package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.WhirlwindDenialSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounterOpponentsSpellsAndAbilitiesUnlessPaysHandler implements MayEffectHandlerBean {

    private final WhirlwindDenialSupport whirlwindDenialSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        whirlwindDenialSupport.handlePaymentChoice(gameData, player, accepted, ability);
    }
}
