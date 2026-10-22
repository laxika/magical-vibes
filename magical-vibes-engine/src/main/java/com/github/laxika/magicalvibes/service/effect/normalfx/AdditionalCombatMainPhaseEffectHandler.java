package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedAdditionalCombatBeginningEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyTargetCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdditionalCombatMainPhaseEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdditionalCombatMainPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AdditionalCombatMainPhaseEffect) effect;
        if (e.count() <= 0) {
            return;
        }
        boolean mainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        if (e.onlyDuringMainPhase() && !mainPhase) {
            return;
        }
        if (e.afterSecondMainPhase() && gameData.postcombatMainPhasesBegunThisTurn > 1) {
            return;
        }
        boolean deferredToSecondMain = e.afterSecondMainPhase() && gameData.currentStep == TurnStep.PRECOMBAT_MAIN;
        if (mainPhase && gameData.additionalCombatMainPhasePairsReturnStep == null) {
            gameData.additionalCombatMainPhasePairsReturnStep = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                    && !e.afterSecondMainPhase() ? TurnStep.BEGINNING_OF_COMBAT : TurnStep.END_STEP;
        }

        gameData.additionalCombatMainPhasePairs += e.count();
        if (deferredToSecondMain) {
            gameData.additionalCombatMainPhasePairsAfterSecondMain += e.count();
        }
        CardEffect additionalCombatBeginningEffect = e.additionalCombatBeginningEffect();
        if (e.onlyTargetCreaturesCanAttack()) {
            CardEffect attackRestriction = new OnlyTargetCreaturesCanAttackThisCombatEffect(
                    Set.copyOf(entry.getTargetIds()));
            additionalCombatBeginningEffect = additionalCombatBeginningEffect == null
                    ? attackRestriction
                    : SequenceEffect.of(additionalCombatBeginningEffect, attackRestriction);
        }
        if (additionalCombatBeginningEffect != null) {
            for (int i = 0; i < e.count(); i++) {
                gameData.queueDelayedAction(new DelayedAdditionalCombatBeginningEffect(
                        entry.getControllerId(), entry.getCard(), additionalCombatBeginningEffect,
                        entry.getSourcePermanentId()));
            }
        }

        String logEntry;
        String afterPhase = e.afterSecondMainPhase() ? "After the second main phase this turn, there" : "After this main phase, there";
        if (e.count() == 1) {
            logEntry = afterPhase + " is an additional combat phase followed by an additional main phase.";
        } else {
            logEntry = afterPhase + " are " + e.count()
                    + " additional combat " + TurnSupport.pluralize("phase", e.count())
                    + " followed by additional main " + TurnSupport.pluralize("phase", e.count()) + ".";
        }
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} queued {} additional combat/main phase pair(s)",
                gameData.id, entry.getCard().getName(), e.count());
    }
}
