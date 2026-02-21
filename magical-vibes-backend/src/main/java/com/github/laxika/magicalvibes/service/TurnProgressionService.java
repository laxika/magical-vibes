package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnProgressionService {

    private final CombatService combatService;
    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    public void advanceStep(GameData gameData) {
        // Process end-of-combat sacrifices when leaving END_OF_COMBAT
        if (gameData.currentStep == TurnStep.END_OF_COMBAT && !gameData.permanentsToSacrificeAtEndOfCombat.isEmpty()) {
            combatService.processEndOfCombatSacrifices(gameData);
            gameData.priorityPassedBy.clear();
            return;
        }

        gameData.priorityPassedBy.clear();
        gameData.interaction.clearAwaitingInput();
        TurnStep next = gameData.currentStep.next();

        // CR 508.8: If no creatures are attacking, skip declare blockers and combat damage
        if (gameData.currentStep == TurnStep.DECLARE_ATTACKERS) {
            List<Integer> attackers = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
            if (attackers.isEmpty()) {
                next = TurnStep.END_OF_COMBAT;
            }
        }

        if (gameData.currentStep == TurnStep.POSTCOMBAT_MAIN && gameData.additionalCombatMainPhasePairs > 0) {
            next = TurnStep.BEGINNING_OF_COMBAT;
            gameData.additionalCombatMainPhasePairs--;
        }

        gameHelper.drainManaPools(gameData);

        if (next != null) {
            gameData.currentStep = next;
            String logEntry = "Step: " + next.getDisplayName();
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Step advanced to {}", gameData.id, next);
            gameBroadcastService.broadcastGameState(gameData);

            if (gameData.status == GameStatus.FINISHED) return;

            if (next == TurnStep.UPKEEP) {
                handleUpkeepTriggers(gameData);
            } else if (next == TurnStep.DRAW) {
                handleDrawStep(gameData);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                combatService.handleDeclareAttackersStep(gameData);
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleCombatResult(combatService.handleDeclareBlockersStep(gameData), gameData);
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            } else if (next == TurnStep.END_OF_COMBAT) {
                combatService.clearCombatState(gameData);
            } else if (next == TurnStep.END_STEP) {
                handleEndStepTriggers(gameData);
            } else if (next == TurnStep.CLEANUP) {
                // CR 514.1: Active player discards down to maximum hand size (normally 7)
                UUID activePlayerId = gameData.activePlayerId;
                List<Card> hand = gameData.playerHands.get(activePlayerId);
                if (hand != null && hand.size() > 7 && !gameHelper.hasNoMaximumHandSize(gameData, activePlayerId)) {
                    int discardCount = hand.size() - 7;
                    gameData.cleanupDiscardPending = true;
                    gameData.discardCausedByOpponent = false;
                    gameData.interaction.setDiscardRemainingCount(discardCount);
                    playerInputService.beginDiscardChoice(gameData, activePlayerId);
                    return;
                }
                // CR 514.2: Remove damage and end "until end of turn" effects
                gameHelper.resetEndOfTurnModifiers(gameData);
            }
        } else {
            advanceTurn(gameData);
        }
    }

    void handleUpkeepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> upkeepEffects = perm.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED);
            if (upkeepEffects == null || upkeepEffects.isEmpty()) continue;

            for (CardEffect effect : upkeepEffects) {
                if (effect instanceof MayEffect may) {
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            activePlayerId,
                            List.of(may.wrapped()),
                            perm.getCard().getName() + " — " + may.prompt()
                    ));
                } else if (effect instanceof WinGameIfCreaturesInGraveyardEffect winEffect) {
                    // Intervening-if: only trigger if condition is met
                    List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
                    long creatureCount = 0;
                    if (graveyard != null) {
                        creatureCount = graveyard.stream()
                                .filter(c -> c.getType() == CardType.CREATURE
                                        || c.getAdditionalTypes().contains(CardType.CREATURE))
                                .count();
                    }
                    if (creatureCount >= winEffect.threshold()) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect))
                        ));

                        String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} creatures in graveyard)",
                                gameData.id, perm.getCard().getName(), creatureCount);
                    }
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect))
                    ));

                    String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> upkeepEffects = card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED);
                if (upkeepEffects == null || upkeepEffects.isEmpty()) continue;

                for (CardEffect effect : upkeepEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                card,
                                activePlayerId,
                                List.of(may.wrapped()),
                                card.getName() + " - " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                activePlayerId,
                                card.getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect))
                        ));

                        String logEntry = card.getName() + "'s upkeep ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} graveyard upkeep trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // Check all battlefields for EACH_UPKEEP_TRIGGERED effects
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> playerBattlefield = gameData.playerBattlefields.get(playerId);
            if (playerBattlefield == null) continue;

            for (Permanent perm : playerBattlefield) {
                List<CardEffect> eachUpkeepEffects = perm.getCard().getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED);
                if (eachUpkeepEffects == null || eachUpkeepEffects.isEmpty()) continue;

                for (CardEffect effect : eachUpkeepEffects) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            (UUID) null
                    ));

                    String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} each-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        // Check all battlefields for OPPONENT_UPKEEP_TRIGGERED effects (only opponents of the active player)
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(activePlayerId)) continue; // Skip the active player's own permanents

            List<Permanent> playerBattlefield = gameData.playerBattlefields.get(playerId);
            if (playerBattlefield == null) continue;

            for (Permanent perm : playerBattlefield) {
                List<CardEffect> opponentUpkeepEffects = perm.getCard().getEffects(EffectSlot.OPPONENT_UPKEEP_TRIGGERED);
                if (opponentUpkeepEffects == null || opponentUpkeepEffects.isEmpty()) continue;

                for (CardEffect effect : opponentUpkeepEffects) {
                    // Intervening-if: check condition at trigger time
                    if (effect instanceof DealDamageIfFewCardsInHandEffect fewCardsEffect) {
                        List<Card> hand = gameData.playerHands.get(activePlayerId);
                        int handSize = hand != null ? hand.size() : 0;
                        if (handSize > fewCardsEffect.maxCards()) {
                            continue; // Condition not met, don't trigger
                        }
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            (UUID) null
                    ));

                    String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} opponent-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        if (!gameData.stack.isEmpty()) {

        }

        playerInputService.processNextMayAbility(gameData);
    }

    void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // The starting player skips their entire draw step on turn 1 (rule 103.7a)
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        // Normal draw (turn-based action, rule 504.1)
        gameHelper.resolveDrawCard(gameData, activePlayerId);

        // Check for draw step triggered abilities (e.g. Howling Mine)
        handleDrawStepTriggers(gameData);

        if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    private void handleDrawStepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // Check active player's battlefield for DRAW_TRIGGERED effects (controller's own draw step only)
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.DRAW_TRIGGERED);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect effect : drawEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                activePlayerId,
                                List.of(may.wrapped()),
                                perm.getCard().getName() + " — " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s draw step ability",
                                new ArrayList<>(List.of(effect)),
                                activePlayerId,
                                perm.getId()
                        ));

                        String logEntry = perm.getCard().getName() + "'s draw step ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} draw-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // Check all battlefields for EACH_DRAW_TRIGGERED effects (all players' draw steps)
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> playerBattlefield = gameData.playerBattlefields.get(playerId);
            if (playerBattlefield == null) continue;

            for (Permanent perm : playerBattlefield) {
                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.EACH_DRAW_TRIGGERED);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect effect : drawEffects) {
                    // Intervening-if: skip trigger if the effect requires an untapped source and it's tapped
                    if (effect instanceof DrawCardForTargetPlayerEffect dcEffect
                            && dcEffect.requireSourceUntapped() && perm.isTapped()) {
                        continue;
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s draw step ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            perm.getId()
                    ));

                    String logEntry = perm.getCard().getName() + "'s draw step ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} draw-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }
    }

    private void handleEndStepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> triggerOrder = new ArrayList<>();
        triggerOrder.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                triggerOrder.add(playerId);
            }
        }

        for (UUID playerId : triggerOrder) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent perm : battlefield) {
                List<CardEffect> endStepEffects = perm.getCard().getEffects(EffectSlot.END_STEP_TRIGGERED);
                if (endStepEffects == null || endStepEffects.isEmpty()) continue;

                for (CardEffect effect : endStepEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                List.of(may.wrapped()),
                                perm.getCard().getName() + " - " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        String logEntry = perm.getCard().getName() + "'s end step ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    void advanceTurn(GameData gameData) {
        UUID nextActive;
        if (!gameData.extraTurns.isEmpty()) {
            nextActive = gameData.extraTurns.pollFirst();
        } else {
            List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
            UUID currentActive = gameData.activePlayerId;
            nextActive = ids.get(0).equals(currentActive) ? ids.get(1) : ids.get(0);
        }
        String nextActiveName = gameData.playerIdToName.get(nextActive);

        gameData.activePlayerId = nextActive;
        gameData.turnNumber++;
        gameData.currentStep = TurnStep.first();
        gameData.interaction.clearAwaitingInput();
        gameData.priorityPassedBy.clear();
        gameData.landsPlayedThisTurn.clear();
        gameData.spellsCastThisTurn.clear();
        gameData.activatedAbilityUsesThisTurn.clear();
        gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.creatureCardsDamagedThisTurnBySourcePermanent.clear();
        gameData.additionalCombatMainPhasePairs = 0;
        gameData.cleanupDiscardPending = false;

        gameHelper.drainManaPools(gameData);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> playerBattlefield = gameData.playerBattlefields.get(playerId);
            if (playerBattlefield == null) continue;
            playerBattlefield.forEach(p -> p.setAttackedThisTurn(false));
        }

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> battlefield = gameData.playerBattlefields.get(nextActive);
        if (battlefield != null) {
            battlefield.forEach(p -> {
                boolean hasAuraDoesntUntap = gameQueryService.hasAuraWithEffect(gameData, p, EnchantedCreatureDoesntUntapEffect.class);
                boolean hasSelfDoesntUntap = p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof DoesntUntapDuringUntapStepEffect);
                if (!hasAuraDoesntUntap && !hasSelfDoesntUntap) {
                    p.untap();
                }
                p.setSummoningSick(false);
                p.setLoyaltyAbilityUsedThisTurn(false);
            });
        }


        String untapLog = nextActiveName + " untaps their permanents.";
        gameBroadcastService.logAndBroadcast(gameData, untapLog);
        log.info("Game {} - {} untaps their permanents", gameData.id, nextActiveName);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(nextActive)) {
                continue;
            }
            if (!controlsUntapOnEachOtherPlayersStep(gameData, playerId, TurnStep.UNTAP)) {
                continue;
            }

            List<Permanent> playerBattlefield = gameData.playerBattlefields.get(playerId);
            if (playerBattlefield == null) {
                continue;
            }
            playerBattlefield.forEach(Permanent::untap);

            String playerName = gameData.playerIdToName.get(playerId);
            String seedbornLog = playerName + " untaps their permanents due to Seedborn Muse.";
            gameBroadcastService.logAndBroadcast(gameData, seedbornLog);
            log.info("Game {} - {} untaps permanents due to Seedborn Muse", gameData.id, playerName);
        }

        String logEntry = "Turn " + gameData.turnNumber + " begins. " + nextActiveName + "'s turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, nextActiveName);
        gameBroadcastService.broadcastGameState(gameData);
    }

    private boolean controlsUntapOnEachOtherPlayersStep(GameData gameData, UUID playerId, TurnStep step) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect configuredEffect
                            && configuredEffect.step() == step);
            if (hasEffect) {
                return true;
            }
        }
        return false;
    }

    void handleCombatResult(CombatResult result, GameData gameData) {
        if (result == CombatResult.ADVANCE_AND_AUTO_PASS || result == CombatResult.ADVANCE_ONLY) {
            advanceStep(gameData);
        }
        if (result == CombatResult.ADVANCE_AND_AUTO_PASS || result == CombatResult.AUTO_PASS_ONLY) {
            resolveAutoPass(gameData);
        }
    }

    public void applyCleanupResets(GameData gameData) {
        gameHelper.resetEndOfTurnModifiers(gameData);
    }

    public void resolveAutoPass(GameData gameData) {
        // Process any pending discard self-triggers before death triggers
        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            gameHelper.processNextDiscardSelfTrigger(gameData);
        }

        // Process any pending targeted death triggers before auto-passing
        if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            gameHelper.processNextDeathTriggerTarget(gameData);
        }

        for (int safety = 0; safety < 100; safety++) {
            if (gameData.interaction.isAwaitingInput()) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
            if (gameData.status == GameStatus.FINISHED) return;

            // When stack is non-empty, never auto-pass — players must explicitly pass
            if (!gameData.stack.isEmpty()) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);

            // If no one holds priority (both already passed), advance the step
            if (priorityHolder == null) {
                advanceStep(gameData);
                continue;
            }

            List<Integer> playable = gameBroadcastService.getPlayableCardIndices(gameData, priorityHolder);
            if (!playable.isEmpty()) {
                // Priority holder can act — stop and let them decide
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Check if current step is in the priority holder's auto-stop set
            java.util.Set<TurnStep> stopSteps = gameData.playerAutoStopSteps.get(priorityHolder);
            if (stopSteps != null && stopSteps.contains(gameData.currentStep)) {
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Priority holder has nothing to play — auto-pass for them
            String playerName = gameData.playerIdToName.get(priorityHolder);
            log.info("Game {} - Auto-passing priority for {} on step {} (no playable cards)",
                    gameData.id, playerName, gameData.currentStep);

            gameData.priorityPassedBy.add(priorityHolder);

            if (gameData.priorityPassedBy.size() >= 2) {
                advanceStep(gameData);
            } else {
                gameBroadcastService.broadcastGameState(gameData);
            }
        }

        // Safety: if we somehow looped 100 times, broadcast current state and stop
        log.warn("Game {} - resolveAutoPass hit safety limit", gameData.id);
        gameBroadcastService.broadcastGameState(gameData);
    }
}
