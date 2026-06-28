package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdditionalCombatMainPhaseEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

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

        String logEntry;
        if (e.count() == 1) {
            logEntry = "After this main phase, there is an additional combat phase followed by an additional main phase.";
        } else {
            logEntry = "After this main phase, there are " + e.count()
                    + " additional combat " + TurnSupport.pluralize("phase", e.count())
                    + " followed by additional main " + TurnSupport.pluralize("phase", e.count()) + ".";
        }
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} queued {} additional combat/main phase pair(s)",
                gameData.id, entry.getCard().getName(), e.count());
    }
}
