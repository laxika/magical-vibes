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
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldPlacementService;
import com.github.laxika.magicalvibes.service.battlefield.AsEntersInteractionService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Unleash: "You may have this permanent enter with an additional +1/+1 counter on it."
 * Resumes entry after the choice so entry triggers see the chosen counters.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnleashHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Autowired @Lazy
    private BattlefieldPlacementService battlefieldPlacementService;

    @Autowired @Lazy
    private AsEntersInteractionService asEntersInteractionService;
    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnleashEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UnleashEffect unleash = ability.effects().stream().filter(UnleashEffect.class::isInstance)
                .map(UnleashEffect.class::cast).findFirst().orElse(null);
        if (unleash != null && unleash.entryRequest() != null) {
            var request = unleash.entryRequest().withUnleashChoice(accepted);
            if (battlefieldEntryBatchSupport.completeNativeChoice(gameData, request)) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            battlefieldPlacementService.place(gameData, request);
            var spell = request.sourceStackEntry();
            asEntersInteractionService.handleCreatureEnteredBattlefield(gameData, request.controllerId(),
                    request.permanent().getCard(), spell == null ? null : spell.getTargetId(),
                    request.permanent().getCastFromZone() == com.github.laxika.magicalvibes.model.Zone.HAND,
                    spell != null && spell.getEtbMode() != null ? spell.getEtbMode() : request.xValue(),
                    request.xValue(), request.kicked(), spell == null ? java.util.List.of() : spell.getTargetIds(),
                    request.repeatedAdditionalCosts(),
                    spell == null ? java.util.List.of() : spell.getConvokeCreatureIds());
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername()
                    + (accepted ? " unleashes " : " declines unleash for "), ability.sourceCard(), "."));
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (accepted) {
            Permanent source = ability.sourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, ability.sourcePermanentId()) : null;
            if (source != null && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
                int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, 1);
                if (placed > 0) {
                    source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                            source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                    permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnCreature(
                            gameData, source, player.getId());
                    permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                            gameData, source, placed);
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
