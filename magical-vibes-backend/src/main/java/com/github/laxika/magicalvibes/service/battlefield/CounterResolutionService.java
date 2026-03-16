package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.StateTriggerService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfControllerPoisonedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Resolves counter-spell effects on the stack.
 *
 * <p>Handles unconditional counters ({@link CounterSpellEffect}) and conditional
 * "counter unless pays" counters ({@link CounterUnlessPaysEffect}), including
 * uncounterable checks and the copy cessation rule (CR 707.10a).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CounterResolutionService {

    private final GraveyardService graveyardService;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final StateTriggerService stateTriggerService;

    /**
     * Resolves an unconditional counter spell (e.g. Cancel, Counterspell).
     *
     * <p>Finds the targeted spell on the stack and counters it. Does nothing if the
     * target is no longer on the stack or is uncounterable.
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter spell being resolved
     */
    @HandlesEffect(CounterSpellEffect.class)
    void resolveCounterSpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        counterSpell(gameData, entry, targetEntry);
    }

    /**
     * Resolves a counter spell that exiles the countered spell instead of putting it
     * into its owner's graveyard (e.g. Dissipate).
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter spell being resolved
     */
    @HandlesEffect(CounterSpellAndExileEffect.class)
    void resolveCounterSpellAndExile(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        counterSpellAndExile(gameData, entry, targetEntry);
    }

    /**
     * Resolves a conditional counter that only counters if the target spell's controller
     * is poisoned (has at least one poison counter). Used by Corrupted Resolve.
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter spell being resolved
     */
    @HandlesEffect(CounterSpellIfControllerPoisonedEffect.class)
    void resolveCounterSpellIfControllerPoisoned(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID targetControllerId = targetEntry.getControllerId();
        int poisonCounters = gameData.playerPoisonCounters.getOrDefault(targetControllerId, 0);
        if (poisonCounters > 0) {
            counterSpell(gameData, entry, targetEntry);
        } else {
            log.info("Game {} - {} controller is not poisoned, spell not countered",
                    gameData.id, targetEntry.getCard().getName());
        }
    }

    /**
     * Resolves a conditional "counter unless pays" effect (e.g. Spiketail Hatchling, Mana Leak).
     *
     * <p>If the targeted spell's controller cannot pay the required mana, the spell is countered
     * immediately. Otherwise, a {@link PendingMayAbility} is queued to ask the controller whether
     * they want to pay.
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter ability/spell being resolved
     * @param effect   the effect carrying the mana amount that must be paid
     */
    @HandlesEffect(CounterUnlessPaysEffect.class)
    void resolveCounterUnlessPays(GameData gameData, StackEntry entry, CounterUnlessPaysEffect effect) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID targetControllerId = targetEntry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        ManaCost cost = new ManaCost("{" + effect.amount() + "}");

        if (!cost.canPay(pool)) {
            counterSpell(gameData, entry, targetEntry);
        } else {
            // Can pay — ask the opponent via the may ability system
            String prompt = "Pay {" + effect.amount() + "} to prevent " + targetEntry.getCard().getName() + " from being countered?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId, List.of(effect), prompt, targetCardId
            ));
            // processNextMayAbility (called by resolveTopOfStack) will set interaction.awaitingInputType() and send the message
        }
    }

    /**
     * Locates the targeted spell on the stack and validates that it can be countered.
     *
     * @param gameData     the current game state
     * @param targetCardId the card ID of the spell being targeted
     * @param counterSource the stack entry of the spell/ability attempting to counter
     * @return the target {@link StackEntry}, or {@code null} if the target is no longer on the
     *         stack or is uncounterable
     */
    private StackEntry findCounterTarget(GameData gameData, UUID targetCardId, StackEntry counterSource) {
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return null;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            return null;
        }

        if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), counterSource)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    counterSource.getCard().getColor().name().toLowerCase());
            return null;
        }

        return targetEntry;
    }

    /**
     * Counters the target spell: removes it from the stack, moves it to the graveyard
     * (unless it is a copy, per CR 707.10a), and broadcasts a log message.
     *
     * @param gameData the current game state
     * @param source   the stack entry of the spell/ability doing the countering
     * @param target   the stack entry of the spell being countered
     */
    private void counterSpell(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        // CR 603.8 — clean up state-trigger tracking when countered
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        // Copies cease to exist per rule 707.10a — skip graveyard
        if (!target.isCopy()) {
            graveyardService.addCardToGraveyard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = target.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }

    /**
     * Counters the target spell and exiles it instead of putting it into its owner's graveyard.
     * Copies cease to exist per CR 707.10a.
     *
     * @param gameData the current game state
     * @param source   the stack entry of the spell/ability doing the countering
     * @param target   the stack entry of the spell being countered
     */
    private void counterSpellAndExile(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        // CR 603.8 — clean up state-trigger tracking when countered
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        // Copies cease to exist per rule 707.10a — skip exile
        if (!target.isCopy()) {
            exileService.exileCard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = target.getCard().getName() + " is countered and exiled.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered and exiled {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }
}


