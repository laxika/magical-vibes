package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMatchingPermanentsUnlessOwnerPaysEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Per-permanent pay-or-be-bounced (Cut the Tethers): "return it to its owner's hand unless that
 * player pays {3}". "That player" is the permanent's OWNER, and that is who the prompt goes to.
 */
@Component
@RequiredArgsConstructor
public class ReturnMatchingPermanentsUnlessOwnerPaysHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnMatchingPermanentsUnlessOwnerPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ReturnMatchingPermanentsUnlessOwnerPaysEffect effect = ability.effects().stream()
                .filter(ReturnMatchingPermanentsUnlessOwnerPaysEffect.class::isInstance)
                .map(ReturnMatchingPermanentsUnlessOwnerPaysEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayPenaltyChoiceHandlerService.handleReturnMatchingPermanentsUnlessOwnerPaysChoice(
                gameData, player, accepted, ability, effect);
    }
}
