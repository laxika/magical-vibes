package com.github.laxika.magicalvibes.service.state;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateBasedActionService {

    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final StateTriggerService stateTriggerService;
    private final LegendRuleService legendRuleService;
    private final com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport battleDefeatSupport;

    private enum DeathReason {
        ZERO_TOUGHNESS, LETHAL_DAMAGE, ZERO_LOYALTY, ZERO_DEFENSE
    }

    private record DeathEntry(Permanent permanent, DeathReason reason) {}

    /**
     * Safety bound on CR 704.3 repetition. Every productive pass removes at least one permanent
     * or zeroes out a counter pair, so a legal game converges long before this; the cap only
     * guards against a removal that unexpectedly leaves the permanent on the battlefield.
     */
    private static final int MAX_SBA_PASSES = 100;

    public void performStateBasedActions(GameData gameData) {
        // CR 704.3-704.4 — all applicable state-based actions are performed as a batch, then the
        // check repeats until none are performed. One pass is not enough: a death can remove a
        // static effect (e.g. an anthem) and make another creature's marked damage newly lethal.
        // Permanents already sent to the graveyard this check are skipped on later passes so a
        // removal that leaves the permanent in place can't be performed twice.
        Set<UUID> processedIds = new HashSet<>();
        boolean anyPerformed;
        int passes = 0;
        do {
            anyPerformed = destroyLethalCreaturesAndPlaneswalkers(gameData, processedIds);

            // CR 704.5a — player with 0 or less life loses the game
            // CR 704.5c — player with ten or more poison counters loses the game
            if (gameOutcomeService.checkWinCondition(gameData)) {
                return;
            }

            anyPerformed |= sacrificeCompletedSagas(gameData, processedIds);
            anyPerformed |= sacrificeCreaturesOnSeraphControlLoss(gameData);
            anyPerformed |= cancelCounters(gameData);

            // CR 704.5n / 704.5q — illegally attached auras die, illegal equipment unattaches
            anyPerformed |= permanentRemovalService.enforceAttachmentLegality(gameData);
        } while (anyPerformed && ++passes < MAX_SBA_PASSES);

        if (passes >= MAX_SBA_PASSES) {
            log.warn("Game {} - state-based actions did not converge after {} passes", gameData.id, passes);
        }

        // CR 704.5j — the legend rule is a state-based action, but performing it needs a player
        // choice, so it can't run synchronously inside the pass loop. Prompt only when no other
        // input flow is active or queued (a second begin would clobber it); a deferred violation
        // is simply re-detected on the next check. After the player chooses,
        // PermanentChoiceBattlefieldHandlerService.handleLegendRule re-runs this service so any
        // remaining state-based actions (and further violations) are processed.
        if (!gameData.interaction.isAwaitingInput()
                && gameData.pendingInteractions.isEmpty()
                && gameData.pendingMayAbilities.isEmpty()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (legendRuleService.checkLegendRule(gameData, playerId)) {
                    return;
                }
            }
        }

        // CR 603.8 — check state-triggered abilities after SBAs
        stateTriggerService.checkStateTriggers(gameData);

        checkEmptyLibraryLoss(gameData);
    }

    private boolean destroyLethalCreaturesAndPlaneswalkers(GameData gameData, Set<UUID> processedIds) {
        List<DeathEntry> toDie = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (processedIds.contains(p.getId())) {
                return;
            }
            if (gameQueryService.isCreature(gameData, p) && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                toDie.add(new DeathEntry(p, DeathReason.ZERO_TOUGHNESS));
            } else if (gameQueryService.isCreature(gameData, p)
                    && (p.getMarkedDamage() >= gameQueryService.getEffectiveToughness(gameData, p)
                            || p.isDamagedByDeathtouch())
                    && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                    && !graveyardService.tryRegenerate(gameData, p)) {
                // CR 704.5g — creature with damage >= toughness is destroyed, and
                // CR 704.5h — creature dealt damage by a deathtouch source since the last check
                // is destroyed (regeneration can replace either)
                toDie.add(new DeathEntry(p, DeathReason.LETHAL_DAMAGE));
            } else if (gameQueryService.isPlaneswalker(gameData, p) && p.getCounterCount(CounterType.LOYALTY) <= 0) {
                toDie.add(new DeathEntry(p, DeathReason.ZERO_LOYALTY));
            } else if (gameQueryService.isBattle(gameData, p) && p.getCounterCount(CounterType.DEFENSE) <= 0
                    && !battleDefeatSupport.hasDefeatTriggerOnStack(gameData, p.getId())) {
                // CR 704.5v — battle with no defense counters is put into the graveyard unless a
                // "when this battle is defeated" ability is still on the stack.
                toDie.add(new DeathEntry(p, DeathReason.ZERO_DEFENSE));
            }
        });

        // CR 704.5h spans "since the last state-based check" and this pass is that check:
        // consume the deathtouch memory so survivors (indestructible, regenerated) aren't
        // re-destroyed by a later pass or a later check.
        gameData.forEachPermanent((playerId, p) -> p.setDamagedByDeathtouch(false));

        try {
            for (DeathEntry entry : toDie) {
                if (gameQueryService.isCreature(gameData, entry.permanent())) {
                    UUID controllerId = gameQueryService.findPermanentController(gameData, entry.permanent().getId());
                    if (controllerId != null) {
                        gameData.simultaneousDyingCreatures.put(entry.permanent().getId(), entry.permanent());
                        gameData.simultaneousDyingControllers.put(entry.permanent().getId(), controllerId);
                    }
                }
            }

            for (DeathEntry entry : toDie) {
                processedIds.add(entry.permanent().getId());
                permanentRemovalService.removePermanentToGraveyard(gameData, entry.permanent());
                Card cardEntry = entry.permanent().getCard();
                String name = cardEntry.getName();
                switch (entry.reason()) {
                    case ZERO_TOUGHNESS -> {
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(cardEntry, " is put into the graveyard (0 toughness)."));
                        log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, name);
                    }
                    case LETHAL_DAMAGE -> {
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(cardEntry, " is destroyed (lethal damage)."));
                        log.info("Game {} - {} dies to state-based actions (lethal damage)", gameData.id, name);
                    }
                    case ZERO_LOYALTY -> {
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(cardEntry, " has no loyalty counters and is put into the graveyard."));
                        log.info("Game {} - {} dies to state-based actions (0 loyalty)", gameData.id, name);
                    }
                    case ZERO_DEFENSE -> {
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(cardEntry, " has no defense counters and is put into the graveyard."));
                        log.info("Game {} - {} dies to state-based actions (0 defense)", gameData.id, name);
                    }
                }
            }
        } finally {
            gameData.simultaneousDyingCreatures.clear();
            gameData.simultaneousDyingControllers.clear();
        }

        if (!toDie.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
        return !toDie.isEmpty();
    }

    // CR 714.4 — Saga with lore counters >= final chapter is sacrificed
    // (unless it has a chapter ability still on the stack)
    private boolean sacrificeCompletedSagas(GameData gameData, Set<UUID> processedIds) {
        List<Permanent> sagasToSacrifice = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (processedIds.contains(p.getId())) return;
            if (!p.getCard().isSaga()) return;
            int finalChapter = p.getCard().getSagaFinalChapter();
            if (finalChapter <= 0 || p.getCounterCount(CounterType.LORE) < finalChapter) return;

            boolean chapterOnStack = gameData.stack.stream()
                    .anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                            && p.getId().equals(e.getSourcePermanentId()));
            if (!chapterOnStack) {
                sagasToSacrifice.add(p);
            }
        });

        for (Permanent saga : sagasToSacrifice) {
            processedIds.add(saga.getId());
            permanentRemovalService.removePermanentToGraveyard(gameData, saga);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(saga.getCard(), " is sacrificed (final chapter reached)."));
            log.info("Game {} - {} sacrificed (lore counters >= final chapter)", gameData.id, saga.getCard().getName());
        }
        return !sagasToSacrifice.isEmpty();
    }

    /**
     * Seraph: "Sacrifice the creature when you lose control of this creature." Each creature Seraph
     * returned is linked to that Seraph. When a Seraph that is still on the battlefield changes
     * controllers, the player who lost control sacrifices the linked creatures they still control.
     * A Seraph that leaves the battlefield never triggers this — its linkage is simply dropped, so
     * the returned creatures stay for good. Modeled as an SBA-timed check (the engine has no
     * control-change triggered-ability slot); rare enough that the loss of stack interaction is
     * acceptable.
     */
    private boolean sacrificeCreaturesOnSeraphControlLoss(GameData gameData) {
        if (gameData.seraphReturnedCreatures.isEmpty()) return false;

        List<Permanent> toSacrifice = new ArrayList<>();
        for (UUID seraphId : new ArrayList<>(gameData.seraphReturnedCreatures.keySet())) {
            if (gameQueryService.findPermanentById(gameData, seraphId) == null) {
                // Seraph left the battlefield: no sacrifice ever; stop tracking.
                gameData.seraphReturnedCreatures.remove(seraphId);
                gameData.seraphControlWatch.remove(seraphId);
                continue;
            }
            java.util.Set<UUID> linked = gameData.seraphReturnedCreatures.get(seraphId);
            linked.removeIf(id -> gameQueryService.findPermanentById(gameData, id) == null);

            UUID currentController = gameData.findControllerOf(seraphId);
            UUID prevController = gameData.seraphControlWatch.get(seraphId);
            if (prevController != null && !prevController.equals(currentController)) {
                for (UUID creatureId : new ArrayList<>(linked)) {
                    Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
                    if (creature != null && prevController.equals(gameData.findControllerOf(creatureId))) {
                        toSacrifice.add(creature);
                        linked.remove(creatureId);
                    }
                }
            }
            gameData.seraphControlWatch.put(seraphId, currentController);
            if (linked.isEmpty()) {
                gameData.seraphReturnedCreatures.remove(seraphId);
                gameData.seraphControlWatch.remove(seraphId);
            }
        }

        for (Permanent creature : toSacrifice) {
            if (permanentRemovalService.removePermanentToGraveyard(gameData, creature)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(creature.getCard(), " is sacrificed (its controller lost control of Seraph)."));
                log.info("Game {} - {} sacrificed (lost control of Seraph)", gameData.id, creature.getCard().getName());
            }
        }
        if (!toSacrifice.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
        return !toSacrifice.isEmpty();
    }

    // CR 704.5q — +1/+1 and -1/-1 counters cancel each other out
    private boolean cancelCounters(GameData gameData) {
        boolean[] anyCancelled = {false};
        gameData.forEachPermanent((pid, p) -> {
            int plus = p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            int minus = p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
            if (plus > 0 && minus > 0) {
                anyCancelled[0] = true;
                int cancelled = Math.min(plus, minus);
                p.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, plus - cancelled);
                p.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, minus - cancelled);
                // Protean Hydra ruling: "its last ability triggers whenever a +1/+1 counter is removed
                // from it for any reason" — SBA counter annihilation triggers regrowth
                if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof DelayedPlusOnePlusOneCounterRegrowthEffect)) {
                    gameData.addDelayedPlusOneCounters(p.getId(), cancelled * 2);
                }
            }
        });
        return anyCancelled[0];
    }

    // CR 704.5b — player who attempted to draw from an empty library loses the game
    private void checkEmptyLibraryLoss(GameData gameData) {
        if (gameData.playersAttemptedDrawFromEmptyLibrary.isEmpty()) return;

        for (UUID playerId : List.copyOf(gameData.playersAttemptedDrawFromEmptyLibrary)) {
            if (gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String logEntry = gameData.playerIdToName.get(playerId) + " attempted to draw from an empty library and loses the game.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} loses (drew from empty library)", gameData.id, gameData.playerIdToName.get(playerId));
                gameOutcomeService.declareWinner(gameData, winnerId);
            }
        }
        gameData.playersAttemptedDrawFromEmptyLibrary.clear();
    }
}
