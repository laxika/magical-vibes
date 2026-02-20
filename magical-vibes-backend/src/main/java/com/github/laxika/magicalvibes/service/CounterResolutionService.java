package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CounterResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(CounterSpellEffect.class,
                (gd, entry, effect) -> resolveCounterSpell(gd, entry));
        registry.register(CounterUnlessPaysEffect.class,
                (gd, entry, effect) -> resolveCounterUnlessPays(gd, entry, (CounterUnlessPaysEffect) effect));
    }

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

        // Copies cease to exist per rule 707.10a — skip graveyard
        if (!targetEntry.isCopy()) {
            UUID ownerId = targetEntry.getControllerId();
            gameHelper.addCardToGraveyard(gameData, ownerId, targetEntry.getCard());
        }

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
            // Copies cease to exist per rule 707.10a — skip graveyard
            if (!targetEntry.isCopy()) {
                gameHelper.addCardToGraveyard(gameData, targetControllerId, targetEntry.getCard());
            }

            String logMsg = targetEntry.getCard().getName() + " is countered.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} countered {} (can't pay {})", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName(), effect.amount());
        } else {
            // Can pay — ask the opponent via the may ability system
            String prompt = "Pay {" + effect.amount() + "} to prevent " + targetEntry.getCard().getName() + " from being countered?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId, List.of(effect), prompt, targetCardId
            ));
            // processNextMayAbility (called by resolveTopOfStack) will set interaction.awaitingInput and send the message
        }
    }
}

