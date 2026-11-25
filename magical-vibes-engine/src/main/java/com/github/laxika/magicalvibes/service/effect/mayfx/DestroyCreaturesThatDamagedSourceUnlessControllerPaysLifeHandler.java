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
 * controller pays 2 life". The unpaid creatures are destroyed together after the last decision (CR 101.4);
 * that answer is completed with the death replacement window open, so a creature with competing death
 * replacements is asked about before it leaves (CR 616.1) and the answer is completed again with that choice.
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
