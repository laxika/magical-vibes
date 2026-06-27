package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EndTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Rule 723.1a: Triggered abilities that haven't been put on the stack yet cease to exist
        gameData.pendingMayAbilities.clear();

        // Rule 723.1b: Exile every object on the stack
        turnSupport.exileStackEntries(gameData);

        // Rule 723.1c: Clear combat state
        turnSupport.clearCombatState(gameData);

        // Rule 723.1d: Skip to cleanup step
        turnSupport.skipToCleanupStep(gameData);

        // Flag so resolveTopOfStack exiles the resolving card instead of graveyard (rule 723.1b)
        gameData.endTurnRequested = true;

        gameBroadcastService.logAndBroadcast(gameData, "The turn ends.");
        log.info("Game {} - End the turn effect resolved, skipping to cleanup", gameData.id);
    }
}
