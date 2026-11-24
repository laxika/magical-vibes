package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhasesAfterMainEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyMatchingCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdditionalCombatPhasesAfterMainEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdditionalCombatPhasesAfterMainEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var additionalCombats = (AdditionalCombatPhasesAfterMainEffect) effect;
        if (additionalCombats.count() <= 0
                || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                    && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)) {
            return;
        }

        gameData.additionalCombatPhasesAfterMain += additionalCombats.count();
        if (gameData.additionalCombatPhasesAfterMainReturnStep == null) {
            gameData.additionalCombatPhasesAfterMainReturnStep =
                    gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                            ? TurnStep.BEGINNING_OF_COMBAT
                            : TurnStep.END_STEP;
        }

        List<CardEffect> beginningEffects = new ArrayList<>();
        if (additionalCombats.additionalCombatBeginningEffect() != null) {
            beginningEffects.add(additionalCombats.additionalCombatBeginningEffect());
        }
        if (additionalCombats.onlyTargetCreaturesCanAttack()) {
            beginningEffects.add(OnlyMatchingCreaturesCanAttackThisCombatEffect.only(Set.copyOf(entry.getTargetIds())));
        }
        AdditionalCombatBeginningSupport.register(gameData, entry, beginningEffects, additionalCombats.count(), 0);

        String logEntry = additionalCombats.count() == 1
                ? "After this main phase, there is an additional combat phase."
                : "After this main phase, there are " + additionalCombats.count()
                        + " additional combat phases.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} queued {} additional combat phase(s) after the main phase",
                gameData.id, entry.getCard().getName(), additionalCombats.count());
    }
}
