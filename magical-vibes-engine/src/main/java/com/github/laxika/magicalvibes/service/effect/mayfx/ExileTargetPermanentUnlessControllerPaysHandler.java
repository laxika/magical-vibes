package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPermanentUnlessControllerPaysEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles the target creature controller's pay-or-exile choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentUnlessControllerPaysHandler implements MayEffectHandlerBean {

    private final ExileTargetPermanentUnlessControllerPaysEffectHandler effectHandler;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentUnlessControllerPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(ability.controllerId());
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + ability.manaCost() + ". (",
                        ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to keep {} on the battlefield", gameData.id,
                        player.getUsername(), ability.manaCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        var sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
        effectHandler.exileTargetPermanent(gameData, ability.sourceCard(), sourceControllerId,
                ability.targetCardId(), ability.sourcePermanentId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
