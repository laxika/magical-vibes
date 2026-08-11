package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles Aether Rift's turn-order life-payment choice after a creature is discarded. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeHandler
        implements MayEffectHandlerBean {

    private final DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffectHandler effectHandler;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect.class::isInstance)
                .map(DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && effectHandler.canPayLife(gameData, player.getId(), effect.lifeCost())) {
            gameData.playerLifeTotals.put(player.getId(),
                    gameData.getLife(player.getId()) - effect.lifeCost());
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + effect.lifeCost() + " life. (",
                    ability.sourceCard(), ")"));
            log.info("Game {} - {} pays {} life to prevent {}'s discarded creature from returning",
                    gameData.id, player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        offerNextOrReturn(gameData, ability, effect);
    }

    private void offerNextOrReturn(GameData gameData, PendingMayAbility ability,
                                   DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect effect) {
        List<UUID> remaining = effect.remainingPayerIds();
        for (int i = 0; i < remaining.size(); i++) {
            UUID payerId = remaining.get(i);
            if (!effectHandler.canPayLife(gameData, payerId, effect.lifeCost())) {
                continue;
            }
            var nextEffect = new DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect(
                    effect.lifeCost(), effect.discardedCardId(), effect.returnControllerId(),
                    remaining.subList(i + 1, remaining.size()));
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    ability.sourceCard(),
                    payerId,
                    List.of(nextEffect),
                    "Pay " + effect.lifeCost() + " life to prevent the discarded creature from returning?",
                    effect.discardedCardId(),
                    null,
                    ability.sourcePermanentId()));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        effectHandler.returnDiscardedCreature(gameData, effect.discardedCardId(),
                effect.returnControllerId(), ability.sourceCard());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
