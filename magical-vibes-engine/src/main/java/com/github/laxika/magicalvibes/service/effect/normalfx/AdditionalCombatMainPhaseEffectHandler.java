package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedAdditionalCombatBeginningEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

        gameData.additionalCombatMainPhasePairs += e.count();
        if (e.additionalCombatBeginningEffect() != null) {
            for (int i = 0; i < e.count(); i++) {
                gameData.queueDelayedAction(new DelayedAdditionalCombatBeginningEffect(
                        entry.getControllerId(), entry.getCard(), e.additionalCombatBeginningEffect()));
            }
        }

        String logEntry;
        if (e.count() == 1) {
            logEntry = "After this main phase, there is an additional combat phase followed by an additional main phase.";
        } else {
            logEntry = "After this main phase, there are " + e.count()
                    + " additional combat " + TurnSupport.pluralize("phase", e.count())
                    + " followed by additional main " + TurnSupport.pluralize("phase", e.count()) + ".";
        }
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} queued {} additional combat/main phase pair(s)",
                gameData.id, entry.getCard().getName(), e.count());
    }
}
