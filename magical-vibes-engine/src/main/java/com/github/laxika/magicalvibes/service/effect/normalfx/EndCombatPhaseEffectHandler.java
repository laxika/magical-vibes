package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EndCombatPhaseEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Resolves the special end-combat instruction used by Mandate of Peace. */
@Component
@RequiredArgsConstructor
public class EndCombatPhaseEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;
    private final ObjectProvider<TurnProgressionService> turnProgressionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EndCombatPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The resolving spell has already left the stack; this flag applies the "including this
        // spell" part when StackResolutionService handles its final disposition.
        entry.setExileInsteadOfGraveyard(true);
        turnSupport.exileStackEntries(gameData, "end the combat phase");
        turnSupport.endCombatPhase(gameData);
        gameLogService.append(gameData, GameLog.text("The combat phase ends."));
        if (gameData.additionalCombatPhasesOnly > 0) {
            // Extra phases are added directly after the specified phase (CR 500.8), so ending the
            // combat phase early moves straight on to the next additional combat phase.
            gameData.currentStep = TurnStep.END_OF_COMBAT;
            turnProgressionService.getObject().advanceStep(gameData);
        }
    }
}
