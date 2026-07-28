package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Destroy-target-creature-unless-controller-pays-toughness-life — "destroy target creature unless
 * its controller pays life equal to its toughness" (e.g. Essence Vortex).
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureUnlessControllerPaysToughnessLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayPenaltyChoiceHandlerService.handleDestroyTargetCreatureUnlessControllerPaysToughnessLifeChoice(
                gameData, player, accepted, ability);
    }
}
