package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect;
import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Handles each attacking creature's pay-or-grant decision. */
@Component
@RequiredArgsConstructor
public class GrantKeywordToBlockingCreaturesUnlessAttackerPaysHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffectHandler normalHandler;
    private final InputCompletionService inputCompletionService;
    private final ResolutionCostPaymentService costPayment;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect effect = ability.effects().stream()
                .filter(GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect.class::isInstance)
                .map(GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect.class::cast)
                .findFirst()
                .orElseThrow();

        boolean paid = false;
        if (accepted) {
            // A tax paid to the ability's controller (Tax Taker).
            if (costPayment.payManaFromPool(gameData, ability.controllerId(), new ManaCost(effect.manaCost()),
                    ability.sourceControllerId())) {
                paid = true;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + effect.manaCost() + ". (", ability.sourceCard(), ")"));
            }
        }

        if (!paid) {
            var sourceControllerId = ability.sourceControllerId() != null
                    ? ability.sourceControllerId()
                    : ability.controllerId();
            StackEntry entry = StackEntry.carrier(ability.sourceCard(),
                    sourceControllerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects()),
                    ability.targetCardId(),
                    ability.sourcePermanentId()
            );
            normalHandler.resolve(gameData, entry, effect);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
