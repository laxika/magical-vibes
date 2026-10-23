package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Destroy-target-creature-unless-controller-pays-toughness-life — "destroy target creature unless
 * its controller pays life equal to its toughness" (e.g. Essence Vortex). A decline only destroys
 * the creature, so a creature with competing death replacements is asked about before it leaves
 * (CR 616.1) and the decline is completed again with that answer.
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureUnlessControllerPaysToughnessLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            mayPenaltyChoiceHandlerService.handleDestroyTargetCreatureUnlessControllerPaysToughnessLifeChoice(
                    gameData, player, true, ability);
            return;
        }
        permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                gameData, player, ability,
                () -> mayPenaltyChoiceHandlerService.handleDestroyTargetCreatureUnlessControllerPaysToughnessLifeChoice(
                        gameData, player, false, ability));
    }
}
