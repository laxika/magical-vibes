package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice for Wernog's triggered ability. */
@Component
@RequiredArgsConstructor
public class EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesHandler
        implements MayEffectHandlerBean {

    private final EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect) ability.effects().getFirst();
        effectHandler.completeChoice(gameData, ability, effect, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
