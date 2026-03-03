package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
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

    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

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

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId);
        if (targetEntry == null) return;

        counterSpell(gameData, entry, targetEntry);
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

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId);
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
     * @param gameData    the current game state
     * @param targetCardId the card ID of the spell being targeted
     * @return the target {@link StackEntry}, or {@code null} if the target is no longer on the
     *         stack or is uncounterable
     */
    private StackEntry findCounterTarget(GameData gameData, UUID targetCardId) {
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

        // Copies cease to exist per rule 707.10a — skip graveyard
        if (!target.isCopy()) {
            gameHelper.addCardToGraveyard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = target.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }
}


