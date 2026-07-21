package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Tap-target-creature-unless-controller-pays-life — "tap target creature unless its controller
 * pays N life" (e.g. Vectis Dominator).
 */
@Component
@RequiredArgsConstructor
public class TapTargetCreatureUnlessControllerPaysLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapTargetCreatureUnlessControllerPaysLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayPenaltyChoiceHandlerService.handleTapTargetCreatureUnlessControllerPaysLifeChoice(gameData, player, accepted, ability);
    }
}
