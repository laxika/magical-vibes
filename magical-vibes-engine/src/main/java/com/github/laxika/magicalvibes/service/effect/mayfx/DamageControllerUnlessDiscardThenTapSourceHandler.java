package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageControllerUnlessDiscardThenTapSourceEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "This creature deals N damage to you unless you discard a card. If it deals damage to you this
 * way, tap it." (Mishra's War Machine) — accept discards a card, decline takes the damage and taps.
 */
@Component
@RequiredArgsConstructor
public class DamageControllerUnlessDiscardThenTapSourceHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageControllerUnlessDiscardThenTapSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayPenaltyChoiceHandlerService.handleDamageControllerUnlessDiscardThenTapChoice(gameData, player, accepted, ability);
    }
}
