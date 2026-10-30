package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EndTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Rule 724.1a: Triggered abilities that haven't been put on the stack yet cease to exist
        gameData.pendingMayAbilities.clear();
        gameData.pendingInteractions.removeIf(PermanentChoiceContext.DeathTriggerTarget.class::isInstance);

        // The resolving spell stays on the stack while it resolves (an instant or sorcery is removed
        // only at the end of its resolution), so take it off the stack here to keep the stack exile from
        // exiling it a second time.
        gameData.removeResolvingSpellFromStack(entry);
        // Rule 724.1b: Exile every object on the stack, including the object that's resolving. This happens
        // before the skip to cleanup, so the cleanup discard prompt sees the spell already in exile.
        turnSupport.exileStackEntries(gameData);
        boolean exileResolvingSpell = !entry.isCopy()
                && (entry.getEntryType() == StackEntryType.SORCERY_SPELL
                || entry.getEntryType() == StackEntryType.INSTANT_SPELL);
        if (exileResolvingSpell) {
            exileService.exileCard(gameData, entry.getOwnerId(), entry.getPhysicalCard());
        }
        // The resolving spell's disposition must not run later (the discard prompt pauses resolution),
        // or it would be put into a graveyard after the exile.
        entry.setSpellDispositionHandled(true);

        // Rule 724.1d: Remove creatures from combat. An "exile at end of combat" delayed action has no
        // stated duration, so it survives the skipped combat and waits for the next end of combat (CR 603.7b).
        // The same holds for an untriggered "sacrifice at end of combat" delayed action.
        var endOfCombatExiles = gameData.drainDelayedActions(DelayedPermanentAction.class,
                action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);
        var endOfCombatSacrifices = gameData.drainDelayedActions(SacrificeAtEndOfCombat.class);
        turnSupport.clearCombatState(gameData);
        endOfCombatExiles.forEach(gameData::queueDelayedAction);
        endOfCombatSacrifices.forEach(gameData::queueDelayedAction);

        // Rule 724.1d: Skip to cleanup step
        turnSupport.skipToCleanupStep(gameData);

        // Flag so resolveTopOfStack exiles the resolving card instead of graveyard (rule 724.1b). A non-copy
        // spell was already exiled above, so the flag is only needed for copies, which cease to exist.
        gameData.endTurnRequested = !exileResolvingSpell;

        gameLogService.append(gameData, GameLog.text("The turn ends."));
        log.info("Game {} - End the turn effect resolved, skipping to cleanup", gameData.id);
    }
}
