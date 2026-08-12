package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SerumPowderEffect;
import com.github.laxika.magicalvibes.service.MulliganService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles Serum Powder's optional mulligan-time hand replacement. */
@Component
@RequiredArgsConstructor
public class SerumPowderHandler implements MayEffectHandlerBean {

    private final MulliganService mulliganService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SerumPowderEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mulliganService.resolveSerumPowderChoice(gameData, player, accepted, ability);
    }
}
