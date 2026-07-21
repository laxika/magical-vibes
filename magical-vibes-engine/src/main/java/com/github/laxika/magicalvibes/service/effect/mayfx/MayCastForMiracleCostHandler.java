package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForMiracleCostEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cast-for-miracle-cost choice during resolution of the miracle triggered ability.
 */
@Component
@RequiredArgsConstructor
public class MayCastForMiracleCostHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastForMiracleCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayCastHandlerService.handleMayCastForMiracleCost(gameData, player, accepted, ability);
    }
}
