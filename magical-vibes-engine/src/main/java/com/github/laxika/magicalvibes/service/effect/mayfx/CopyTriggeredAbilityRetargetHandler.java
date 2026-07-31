package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityRetargetEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Copy-triggered-ability retarget — you may choose a new target for a copied triggered ability
 * (e.g. Strionic Resonator).
 */
@Component
@RequiredArgsConstructor
public class CopyTriggeredAbilityRetargetHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTriggeredAbilityRetargetEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayCopyHandlerService.handleCopyTriggeredAbilityRetargetChoice(gameData, player, accepted, ability);
    }
}
