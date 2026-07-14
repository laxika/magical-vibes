package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyActivatedAbilityRetargetEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Copy-activated-ability retarget — you may choose a new target for a copied ability
 * (e.g. Rings of Brighthearth).
 */
@Component
@RequiredArgsConstructor
public class CopyActivatedAbilityRetargetHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyActivatedAbilityRetargetEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CopyActivatedAbilityRetargetEffect effect = ability.effects().stream()
                .filter(e -> e instanceof CopyActivatedAbilityRetargetEffect)
                .map(e -> (CopyActivatedAbilityRetargetEffect) e)
                .findFirst().orElse(null);
        mayCopyHandlerService.handleCopyActivatedAbilityRetargetChoice(gameData, player, accepted, ability, effect);
    }
}
