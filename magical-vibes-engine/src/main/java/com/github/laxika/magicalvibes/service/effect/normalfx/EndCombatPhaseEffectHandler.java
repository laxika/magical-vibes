package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EndCombatPhaseEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the special end-combat instruction used by Mandate of Peace. */
@Component
@RequiredArgsConstructor
public class EndCombatPhaseEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;

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
    }
}
