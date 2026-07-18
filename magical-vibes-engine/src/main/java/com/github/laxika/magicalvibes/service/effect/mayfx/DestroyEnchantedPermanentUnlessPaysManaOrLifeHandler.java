package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Destroy the enchanted permanent unless you pay {M} or N life" (Erosion). Accepting spends the
 * cheaper resource available (floating mana, otherwise life); declining destroys the permanent.
 */
@Component
@RequiredArgsConstructor
public class DestroyEnchantedPermanentUnlessPaysManaOrLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayPenaltyChoiceHandlerService.handleDestroyEnchantedPermanentUnlessPaysChoice(gameData, player, accepted, ability);
    }
}
