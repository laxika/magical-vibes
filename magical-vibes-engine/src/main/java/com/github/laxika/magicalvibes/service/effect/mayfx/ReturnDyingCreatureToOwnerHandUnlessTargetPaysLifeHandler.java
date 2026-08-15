package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes Athreos's targeted choice to pay life or return the dead creature. */
@Component
@RequiredArgsConstructor
public class ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeHandler implements MayEffectHandlerBean {

    private final ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffectHandler effectHandler;
    private final LifeSupport lifeSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect effect = ability.effects().stream()
                .filter(ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect.class::isInstance)
                .map(ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID playerId = ability.controllerId();
        if (accepted && effectHandler.canPay(gameData, playerId, effect.lifeCost())) {
            lifeSupport.applyLifeLoss(gameData, playerId, effect.lifeCost(), ability.sourceCard().getName());
        } else {
            effectHandler.returnDyingCard(gameData, effect.dyingCardId(), ability.sourceCard());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
