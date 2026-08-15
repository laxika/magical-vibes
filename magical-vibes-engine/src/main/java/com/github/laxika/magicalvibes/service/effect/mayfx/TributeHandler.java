package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the opponent's tribute choice and resumes the entering creature's ETB triggers. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TributeHandler implements MayEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TributeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        TributeEffect tribute = ability.effects().stream()
                .filter(TributeEffect.class::isInstance)
                .map(TributeEffect.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tribute choice is missing its effect"));
        Permanent source = ability.sourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());

        boolean paid = false;
        if (source != null && accepted && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
            int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, tribute.counterCount());
            if (placed > 0) {
                source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, source);
                paid = placed >= tribute.counterCount();
            }
        }
        if (source != null) {
            source.setTributePaid(paid);
        }

        if (paid) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays tribute to ", ability.sourceCard(), "."));
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines tribute for ", ability.sourceCard(), "."));
        }
        log.info("Game {} - {} {} tribute for {}", gameData.id, player.getUsername(),
                paid ? "pays" : "declines", ability.sourceCard().getName());

        boolean wasCastFromHand = source != null && source.isCast() && source.getCastFromZone() == Zone.HAND;
        battlefieldEntryService.processCreatureETBEffects(
                gameData, ability.controllerId(), ability.sourceCard(), null, wasCastFromHand);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
