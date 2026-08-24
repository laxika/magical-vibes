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
import org.springframework.stereotype.Component;

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
        if (source != null && accepted) {
            if (!gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
                int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, 1);
                if (placed > 0) {
                    source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                            source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                    permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                            gameData, source);
                    permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                            gameData, source, placed, player.getId());
                }
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses a +1/+1 counter for ", ability.sourceCard(), "."));
        } else if (source != null) {
            if (!gameQueryService.cantHaveOrGainKeyword(gameData, source, Keyword.HASTE)) {
                source.getPersistentGrantedKeywords().add(Keyword.HASTE);
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses haste for ", ability.sourceCard(), "."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
