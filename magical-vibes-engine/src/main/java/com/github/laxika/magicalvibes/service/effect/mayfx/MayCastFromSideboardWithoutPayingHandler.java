package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromSideboardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the single free-cast choice created from a targeted opponent's sideboard. */
@Component
@RequiredArgsConstructor
public class MayCastFromSideboardWithoutPayingHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastFromSideboardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastFromSideboardWithoutPayingManaCostEffect effect = ability.effects().stream()
                .filter(MayCastFromSideboardWithoutPayingManaCostEffect.class::isInstance)
                .map(MayCastFromSideboardWithoutPayingManaCostEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayCastHandlerService.handleCastFromSideboardWithoutPayingManaCost(
                gameData, player, accepted, ability, effect.sideboardOwnerId());
    }
}
