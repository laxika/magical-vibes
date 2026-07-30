package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Per-damaging-creature pay-or-be-destroyed (Giant Albatross): "destroy that creature unless its
 * controller pays 2 life".
 */
@Component
@RequiredArgsConstructor
public class DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

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
        mayPenaltyChoiceHandlerService.handleDestroyCreaturesThatDamagedSourceUnlessPaysLifeChoice(
                gameData, player, accepted, ability, effect);
    }
}
