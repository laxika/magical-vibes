package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Riot's choice of a +1/+1 counter or permanent haste as a creature enters. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiotHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RiotEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Permanent source = ability.sourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        boolean choseCounter = accepted && source != null
                && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source);
        boolean counterPlaced = false;
        if (choseCounter) {
            int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, 1);
            if (placed > 0) {
                source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, source, placed);
                permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                        gameData, source, placed, player.getId());
                counterPlaced = true;
            }
        }
        if (!counterPlaced && source != null) {
            source.getGrantedKeywords().add(Keyword.HASTE);
            source.getPersistentGrantedKeywords().add(Keyword.HASTE);
        }

        if (counterPlaced) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses a +1/+1 counter for ", ability.sourceCard(), "."));
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses haste for ", ability.sourceCard(), "."));
        }
        log.info("Game {} - {} resolves Riot for {} as {}", gameData.id, player.getUsername(),
                ability.sourceCard().getName(), counterPlaced ? "+1/+1 counter" : "haste");
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
