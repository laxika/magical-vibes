package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EndTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Rule 724.1a: Triggered abilities that haven't been put on the stack yet cease to exist
        gameData.pendingMayAbilities.clear();

        // Rule 724.1b: Exile every object on the stack
        turnSupport.exileStackEntries(gameData);

        // Rule 724.1d: Remove creatures from combat. An "exile at end of combat" delayed action has no
        // stated duration, so it survives the skipped combat and waits for the next end of combat (CR 603.7b).
        var endOfCombatExiles = gameData.drainDelayedActions(DelayedPermanentAction.class,
                action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);
        turnSupport.clearCombatState(gameData);
        endOfCombatExiles.forEach(gameData::queueDelayedAction);

        // Rule 724.1d: Skip to cleanup step
        turnSupport.skipToCleanupStep(gameData);

        // Flag so resolveTopOfStack exiles the resolving card instead of graveyard (rule 724.1b)
        gameData.endTurnRequested = true;

        gameLogService.append(gameData, GameLog.text("The turn ends."));
        log.info("Game {} - End the turn effect resolved, skipping to cleanup", gameData.id);
    }
}
