package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class CounterResolutionService {

    private final GameBroadcastService gameBroadcastService;

    void resolveCounterSpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return;
        }

        gameData.stack.remove(targetEntry);

        UUID ownerId = targetEntry.getControllerId();
        gameData.playerGraveyards.get(ownerId).add(targetEntry.getCard());

        String logMsg = targetEntry.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());
    }

    void resolveCounterUnlessPays(GameData gameData, StackEntry entry, CounterUnlessPaysEffect effect) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-pays target no longer on stack", gameData.id);
            return;
        }

        UUID targetControllerId = targetEntry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        ManaCost cost = new ManaCost("{" + effect.amount() + "}");

        if (!cost.canPay(pool)) {
            // Can't pay — counter immediately
            gameData.stack.remove(targetEntry);
            gameData.playerGraveyards.get(targetControllerId).add(targetEntry.getCard());

            String logMsg = targetEntry.getCard().getName() + " is countered.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} countered {} (can't pay {})", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName(), effect.amount());
        } else {
            // Can pay — ask the opponent via the may ability system
            String prompt = "Pay {" + effect.amount() + "} to prevent " + targetEntry.getCard().getName() + " from being countered?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId, List.of(effect), prompt, targetCardId
            ));
            // processNextMayAbility (called by resolveTopOfStack) will set awaitingInput and send the message
        }
    }
}
