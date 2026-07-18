package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TempestEfreetAnteExchangeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TempestEfreetAnteExchangeEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Tempest Efreet ante ability — "target opponent may pay 10 life. If that player doesn't, they reveal
 * a card at random and exchange it for Tempest Efreet." The targeted opponent is the decision maker;
 * accept-and-can-pay costs them the life, otherwise the exchange is performed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempestEfreetAnteExchangeHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;
    private final TempestEfreetAnteExchangeEffectHandler exchangeEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TempestEfreetAnteExchangeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (TempestEfreetAnteExchangeEffect) ability.effects().getFirst();
        UUID opponentId = ability.controllerId();     // the targeted opponent — the decision maker
        UUID controllerId = ability.targetCardId();   // the ability's controller who takes the card

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, opponentId)
                && gameData.getLife(opponentId) >= effect.lifeCost();

        if (accepted && canPay) {
            gameData.playerLifeTotals.put(opponentId, gameData.getLife(opponentId) - effect.lifeCost());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " pays " + effect.lifeCost() + " life. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} pays {} life to avoid the {} exchange", gameData.id,
                    player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        } else {
            // Declined (or can no longer pay) — perform the exchange.
            exchangeEffectHandler.performExchange(gameData, ability.sourceCard(), controllerId, opponentId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
