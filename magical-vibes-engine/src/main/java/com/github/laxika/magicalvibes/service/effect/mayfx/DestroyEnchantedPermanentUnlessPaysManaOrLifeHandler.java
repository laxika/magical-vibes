package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * "Destroy the enchanted permanent unless you pay {M} or N life" (Erosion). Accepting spends the
 * cheaper resource available (floating mana, otherwise life); declining destroys the permanent.
 * A decline only destroys, so a creature land with competing death replacements is asked about
 * before it leaves (CR 616.1) and the decline is completed again with that answer.
 */
@Component
@RequiredArgsConstructor
public class DestroyEnchantedPermanentUnlessPaysManaOrLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            mayPenaltyChoiceHandlerService.handleDestroyEnchantedPermanentUnlessPaysChoice(gameData, player, true, ability);
            return;
        }
        permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                gameData, player, ability,
                () -> mayPenaltyChoiceHandlerService.handleDestroyEnchantedPermanentUnlessPaysChoice(
                        gameData, player, false, ability));
    }
}
