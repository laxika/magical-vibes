package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyAbilityRetargetEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the optional retargeting of a copied activated or triggered ability. */
@Component
@RequiredArgsConstructor
public class CopyAbilityRetargetHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyAbilityRetargetEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayCopyHandlerService.handleCopyAbilityRetargetChoice(gameData, player, accepted, ability);
    }
}
