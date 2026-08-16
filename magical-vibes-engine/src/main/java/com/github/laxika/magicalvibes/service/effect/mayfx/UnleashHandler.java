package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Unleash (CR 702.98a): "You may have this permanent enter with an additional +1/+1 counter on it."
 * Accepting puts one +1/+1 counter on the just-entered permanent; declining leaves it alone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnleashHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnleashEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            Permanent source = ability.sourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, ability.sourcePermanentId()) : null;
            if (source != null && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
                int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, 1);
                if (placed > 0) {
                    source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                            source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                    permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, source);
                    permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                            gameData, source, placed, player.getId());
                }
            }
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " unleashes ", ability.sourceCard(), " (+1/+1 counter)."));
            log.info("Game {} - {} unleashes {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines unleash for ", ability.sourceCard(), "."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
