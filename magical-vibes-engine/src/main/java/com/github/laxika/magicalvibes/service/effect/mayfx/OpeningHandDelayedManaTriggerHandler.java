package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Opening-hand delayed mana trigger — you may reveal this card from your opening hand
 * (e.g. Chancellor of the Tangle). The main-path (pregame) invocation; the resolution-time-from-stack
 * variant stays in {@code MayAbilityHandlerService.handleResolutionTimeMayChoice}.
 */
@Component
@RequiredArgsConstructor
public class OpeningHandDelayedManaTriggerHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedManaTriggerEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RegisterDelayedManaTriggerEffect effect = ability.effects().stream()
                .filter(e -> e instanceof RegisterDelayedManaTriggerEffect)
                .map(e -> (RegisterDelayedManaTriggerEffect) e)
                .findFirst().orElse(null);
        mayMiscHandlerService.handleOpeningHandDelayedManaTrigger(gameData, player, accepted, ability, effect);
    }
}
