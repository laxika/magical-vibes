package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMatchingPermanentsUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Per-permanent pay-or-be-bounced (Cut the Tethers): "return it to its owner's hand unless that
 * player pays {3}".
 */
@Component
@RequiredArgsConstructor
public class ReturnMatchingPermanentsUnlessControllerPaysHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnMatchingPermanentsUnlessControllerPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ReturnMatchingPermanentsUnlessControllerPaysEffect effect = ability.effects().stream()
                .filter(ReturnMatchingPermanentsUnlessControllerPaysEffect.class::isInstance)
                .map(ReturnMatchingPermanentsUnlessControllerPaysEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayPenaltyChoiceHandlerService.handleReturnMatchingPermanentsUnlessControllerPaysChoice(
                gameData, player, accepted, ability, effect);
    }
}
