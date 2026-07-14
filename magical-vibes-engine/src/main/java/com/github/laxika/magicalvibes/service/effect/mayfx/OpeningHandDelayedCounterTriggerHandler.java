package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Opening-hand delayed counter trigger — you may reveal this card from your opening hand
 * (e.g. Chancellor of the Annex). The main-path (pregame) invocation; the resolution-time-from-stack
 * variant stays in {@code MayAbilityHandlerService.handleResolutionTimeMayChoice}.
 */
@Component
@RequiredArgsConstructor
public class OpeningHandDelayedCounterTriggerHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCounterTriggerEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RegisterDelayedCounterTriggerEffect effect = ability.effects().stream()
                .filter(e -> e instanceof RegisterDelayedCounterTriggerEffect)
                .map(e -> (RegisterDelayedCounterTriggerEffect) e)
                .findFirst().orElse(null);
        mayMiscHandlerService.handleOpeningHandDelayedCounterTrigger(gameData, player, accepted, ability, effect);
    }
}
