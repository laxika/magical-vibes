package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Per-damaging-creature pay-or-be-destroyed (Giant Albatross): "destroy that creature unless its
 * controller pays 2 life". A decline pays nothing and destroys the creature before it offers the next
 * one, so a creature with competing death replacements is asked about before it leaves (CR 616.1) and
 * the decline is completed again with that answer.
 */
@Component
@RequiredArgsConstructor
public class DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect effect = ability.effects().stream()
                .filter(DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect.class::isInstance)
                .map(DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (accepted) {
            mayPenaltyChoiceHandlerService.handleDestroyCreaturesThatDamagedSourceUnlessPaysLifeChoice(
                    gameData, player, true, ability, effect);
            return;
        }
        permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                gameData, player, ability,
                () -> mayPenaltyChoiceHandlerService.handleDestroyCreaturesThatDamagedSourceUnlessPaysLifeChoice(
                        gameData, player, false, ability, effect));
    }
}
