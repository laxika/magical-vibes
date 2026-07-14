package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Redirect retarget — you may choose new targets for a target spell (e.g. Redirect). This is the
 * main-path invocation; the resolution-time-from-stack variant (which must advance the pending
 * resolution index for follow-on effects like Wild Ricochet's copy) stays in
 * {@code MayAbilityHandlerService.handleResolutionTimeMayChoice}.
 */
@Component
@RequiredArgsConstructor
public class RedirectRetargetHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNewTargetsForTargetSpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayCopyHandlerService.handleRedirectRetargetChoice(gameData, player, accepted, ability);
    }
}
