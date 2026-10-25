package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Handles one opponent's choice for Wernog's triggered ability. A declined answer loses the life before anything else
 * changes, so a Lich's Duel Mastery that loss would sacrifice is asked about before any life is lost (CR 616.1) and
 * the answer is completed again with that choice.
 */
@Component
@RequiredArgsConstructor
public class EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesHandler
        implements MayEffectHandlerBean {

    private final EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect) ability.effects().getFirst();
        if (accepted) {
            effectHandler.completeChoice(gameData, ability, effect, true);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, false, () -> {
                    effectHandler.completeChoice(gameData, ability, effect, false);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                });
    }
}
