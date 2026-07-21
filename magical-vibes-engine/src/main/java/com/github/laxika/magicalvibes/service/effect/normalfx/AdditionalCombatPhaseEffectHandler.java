package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdditionalCombatPhaseEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdditionalCombatPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AdditionalCombatPhaseEffect) effect;
        if (e.count() <= 0) {
            return;
        }

        gameData.additionalCombatPhasesOnly += e.count();

        String logEntry;
        if (e.count() == 1) {
            logEntry = "After this phase, there is an additional combat phase.";
        } else {
            logEntry = "After this phase, there are " + e.count()
                    + " additional combat " + TurnSupport.pluralize("phase", e.count()) + ".";
        }
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} queued {} additional combat phase(s)",
                gameData.id, entry.getCard().getName(), e.count());
    }
}
