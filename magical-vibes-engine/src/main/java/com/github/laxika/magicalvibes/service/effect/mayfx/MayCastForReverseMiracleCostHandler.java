package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForReverseMiracleCostEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReverseMiracleSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the reverse-miracle choice and resumes the interrupted library search. */
@Component
@RequiredArgsConstructor
public class MayCastForReverseMiracleCostHandler implements MayEffectHandlerBean {

    private final ReverseMiracleSupport reverseMiracleSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastForReverseMiracleCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        reverseMiracleSupport.handleChoice(gameData, player, accepted, ability);
    }
}
