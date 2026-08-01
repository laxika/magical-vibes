package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Completes an as-enters choice that offers a life payment to keep the permanent untapped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayPayLifeOrEntersTappedHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeOrEntersTappedEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayPayLifeOrEntersTappedEffect effect = ability.effects().stream()
                .filter(MayPayLifeOrEntersTappedEffect.class::isInstance)
                .map(MayPayLifeOrEntersTappedEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID playerId = ability.controllerId();
        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= effect.lifeCost();
        if (accepted && canPay) {
            gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - effect.lifeCost());
            triggerCollectionService.checkLifeLossTriggers(gameData, playerId, effect.lifeCost());
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + effect.lifeCost() + " life for ", ability.sourceCard(), "."));
            log.info("Game {} - {} pays {} life for {}", gameData.id, player.getUsername(),
                    effect.lifeCost(), ability.sourceCard().getName());
        } else {
            Permanent source = ability.sourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            if (source != null) {
                source.tap();
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines — ", ability.sourceCard(), " enters tapped."));
            log.info("Game {} - {} declines to pay life; {} enters tapped", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
