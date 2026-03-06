package com.github.laxika.magicalvibes.service;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NoOtherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnProgressionService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CombatService combatService;
    private final DrawService drawService;
    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final PermanentRemovalService permanentRemovalService;
    private final AuraAttachmentService auraAttachmentService;
    private final StackResolutionService stackResolutionService;

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
            } else if (next == TurnStep.PRECOMBAT_MAIN) {
                handlePrecombatMainTriggers(gameData);
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
                auraAttachmentService.returnStolenCreatures(gameData, true);
            }
        } else {
            advanceTurn(gameData);
        }
    }

    void handleUpkeepTriggers(GameData gameData) {
        // Chancellor cycle: at the beginning of the first upkeep, check all players' hands
        // for cards with ON_OPENING_HAND_REVEAL effects (revealed from opening hand)
        if (gameData.turnNumber == 1) {
            handleOpeningHandTriggers(gameData);
        }

        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> upkeepEffects = perm.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED);
            if (upkeepEffects == null || upkeepEffects.isEmpty()) continue;

            for (CardEffect effect : upkeepEffects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                } else if (effect instanceof BecomeCopyOfTargetCreatureEffect) {
                    // Targeted upkeep trigger: target is chosen at trigger time (CR 603.3d).
                    // Collect valid creature targets excluding self ("another creature").
                    boolean hasValidTargets = false;
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> bf = gameData.playerBattlefields.get(pid);
                        if (bf == null) continue;
                        for (Permanent p : bf) {
                            if (p.getId().equals(perm.getId())) continue;
                            if (gameQueryService.isCreature(gameData, p)) {
                                hasValidTargets = true;
                                break;
                            }
                        }
                        if (hasValidTargets) break;
                    }
                    if (hasValidTargets) {
                        gameData.pendingUpkeepCopyTargets.add(new PermanentChoiceContext.UpkeepCopyTriggerTarget(
                                perm.getCard(), activePlayerId, perm.getId()));
                    }
                } else if (effect instanceof NoOtherSubtypeConditionalEffect noOtherSubtype) {
                    // Intervening-if: only trigger if controller has no other permanents with the subtype
                    boolean hasOtherWithSubtype = battlefield.stream()
                            .anyMatch(p -> !p.getId().equals(perm.getId())
                                    && gameQueryService.matchesPermanentPredicate(gameData, p,
                                    new PermanentHasSubtypePredicate(noOtherSubtype.subtype())));
                    if (!hasOtherWithSubtype) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: no other {}s)",
                                gameData.id, perm.getCard().getName(), noOtherSubtype.subtype().getDisplayName());
                    }
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
                            new ArrayList<>(List.of(effect)),
                            (UUID) null,
                            perm.getId()
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
                    CardEffect innerEffect = effect;

                    // Unwrap MetalcraftConditionalEffect — check metalcraft before offering the ability
                    if (innerEffect instanceof MetalcraftConditionalEffect metalcraft) {
                        if (!gameQueryService.isMetalcraftMet(gameData, activePlayerId)) {
                            log.info("Game {} - {} graveyard metalcraft ability skipped (fewer than three artifacts)",
                                    gameData.id, card.getName());
                            continue;
                        }
                        innerEffect = metalcraft.wrapped();
                    }

                    if (innerEffect instanceof MayPayManaEffect mayPay) {
                        gameData.queueMayAbility(card, activePlayerId, mayPay, null);
                    } else if (innerEffect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, activePlayerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                activePlayerId,
                                card.getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(innerEffect))
                        ));

                        String logEntry = card.getName() + "'s upkeep ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} graveyard upkeep trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // Check all battlefields for EACH_UPKEEP_TRIGGERED effects
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> eachUpkeepEffects = perm.getCard().getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED);
            if (eachUpkeepEffects == null || eachUpkeepEffects.isEmpty()) return;

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
        });

        // Check all battlefields for OPPONENT_UPKEEP_TRIGGERED effects (only opponents of the active player)
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return; // Skip the active player's own permanents

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
        });

        // Process upkeep copy trigger target selection first (mandatory targeting at trigger time)
        if (!gameData.pendingUpkeepCopyTargets.isEmpty()) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    public void processNextUpkeepCopyTarget(GameData gameData) {
        if (gameData.pendingUpkeepCopyTargets.isEmpty()) {
            // All copy triggers targeted, continue with may abilities
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepCopyTriggerTarget trigger = gameData.pendingUpkeepCopyTargets.peekFirst();

        // Collect valid creature targets (excluding source permanent)
        List<UUID> validTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(trigger.sourcePermanentId())) continue;
                if (gameQueryService.isCreature(gameData, p)) {
                    validTargets.add(p.getId());
                }
            }
        }

        if (validTargets.isEmpty()) {
            // No valid targets remaining — skip
            gameData.pendingUpkeepCopyTargets.removeFirst();
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        gameData.pendingUpkeepCopyTargets.removeFirst();
        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + " — Choose a creature to target.");

        String logEntry = trigger.sourceCard().getName() + "'s upkeep ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} upkeep copy trigger awaiting target selection", gameData.id, trigger.sourceCard().getName());
    }

    private void handleOpeningHandTriggers(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) continue;

            for (Card card : hand) {
                List<CardEffect> openingHandEffects = card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL);
                if (openingHandEffects == null || openingHandEffects.isEmpty()) continue;

                for (CardEffect effect : openingHandEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, playerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                playerId,
                                card.getName() + "'s opening hand ability",
                                new ArrayList<>(List.of(effect))
                        ));

                        String playerName = gameData.playerIdToName.get(playerId);
                        String logEntry = playerName + " reveals " + card.getName() + " from their opening hand.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} reveals {} from opening hand, trigger pushed onto stack",
                                gameData.id, playerName, card.getName());
                    }
                }
            }
        }
    }

    private void handlePrecombatMainTriggers(GameData gameData) {
        // Chancellor-style delayed mana triggers: fire at the beginning of the revealing player's first main phase
        if (!gameData.openingHandManaTriggers.isEmpty()) {
            UUID activePlayerId = gameData.activePlayerId;
            List<OpeningHandRevealTrigger> toFire = gameData.openingHandManaTriggers.stream()
                    .filter(t -> t.revealingPlayerId().equals(activePlayerId))
                    .toList();

            if (!toFire.isEmpty()) {
                gameData.openingHandManaTriggers.removeAll(toFire);
                for (OpeningHandRevealTrigger trigger : toFire) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            trigger.sourceCard(),
                            trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(trigger.effect()))
                    ));

                    String logEntry = trigger.sourceCard().getName() + "'s delayed trigger fires — adds mana.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {}'s opening hand mana trigger fires for {}",
                            gameData.id, trigger.sourceCard().getName(),
                            gameData.playerIdToName.get(activePlayerId));
                }
            }
        }
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
        drawService.resolveDrawCard(gameData, activePlayerId);

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
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
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
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.EACH_DRAW_TRIGGERED);
            if (drawEffects == null || drawEffects.isEmpty()) return;

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
        });
    }

    private void handleEndStepTriggers(GameData gameData) {
        // Process pending token exiles (e.g. Mimic Vat tokens)
        if (!gameData.pendingTokenExilesAtEndStep.isEmpty()) {
            Set<UUID> toExile = new HashSet<>(gameData.pendingTokenExilesAtEndStep);
            gameData.pendingTokenExilesAtEndStep.clear();
            for (UUID permId : toExile) {
                Permanent token = gameQueryService.findPermanentById(gameData, permId);
                if (token != null) {
                    permanentRemovalService.removePermanentToExile(gameData, token);
                    String logEntry = token.getCard().getName() + " token is exiled.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} token exiled at end step (Mimic Vat)", gameData.id, token.getCard().getName());
                    permanentRemovalService.removeOrphanedAuras(gameData);
                }
            }
        }

        // Process pending exile returns (e.g. Argent Sphinx)
        if (!gameData.pendingExileReturns.isEmpty()) {
            List<PendingExileReturn> returns = new ArrayList<>(gameData.pendingExileReturns);
            gameData.pendingExileReturns.clear();
            for (PendingExileReturn pending : returns) {
                Card card = pending.card();
                UUID controllerId = pending.controllerId();
                // Remove card from exile zone
                List<Card> exiledCards = gameData.playerExiledCards.get(controllerId);
                if (exiledCards != null) {
                    exiledCards.remove(card);
                }
                // Return as a new permanent
                Permanent perm = new Permanent(card);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
                String playerName = gameData.playerIdToName.get(controllerId);
                String logEntry = card.getName() + " returns to the battlefield under " + playerName + "'s control.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returns from exile for {}", gameData.id, card.getName(), playerName);
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
            }
        }

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
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
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
        // Clear any active mind control from the ending turn
        gameData.mindControlledPlayerId = null;
        gameData.mindControllerPlayerId = null;

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

        // Check for pending Mindslaver control on the new active player
        UUID pendingController = gameData.pendingTurnControl.remove(nextActive);
        if (pendingController != null && gameData.playerIds.contains(pendingController)) {
            gameData.mindControlledPlayerId = nextActive;
            gameData.mindControllerPlayerId = pendingController;
            String controllerName = gameData.playerIdToName.get(pendingController);
            String controlLog = controllerName + " controls " + nextActiveName + " this turn (Mindslaver).";
            gameBroadcastService.logAndBroadcast(gameData, controlLog);
            log.info("Game {} - {} controls {} this turn (Mindslaver)", gameData.id, controllerName, nextActiveName);
        }
        gameData.turnNumber++;
        gameData.currentStep = TurnStep.first();
        gameData.interaction.clearAwaitingInput();
        gameData.priorityPassedBy.clear();
        gameData.landsPlayedThisTurn.clear();
        gameData.permanentsEnteredBattlefieldThisTurn.clear();
        gameData.spellsCastThisTurn.clear();
        gameData.activatedAbilityUsesThisTurn.clear();
        gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.clear();
        gameData.creatureDeathCountThisTurn.clear();
        gameData.cardsDrawnThisTurn.clear();
        gameData.combatDamageToPlayersThisTurn.clear();
        gameData.creatureCardsDamagedThisTurnBySourcePermanent.clear();
        gameData.creatureGivingControllerPoisonOnDeathThisTurn.clear();
        gameData.additionalCombatMainPhasePairs = 0;
        gameData.cleanupDiscardPending = false;
        gameData.paidSearchTaxPermanentIds.clear();

        gameHelper.drainManaPools(gameData);

        gameData.forEachPermanent((playerId, p) -> p.setAttackedThisTurn(false));

        // Clean up stale untap-prevention locks on ALL battlefields before untapping.
        // A lock is stale if the source permanent is no longer on the battlefield or is no longer tapped.
        gameData.forEachPermanent((pid, p) -> {
            if (p.getUntapPreventedByPermanentIds().isEmpty()) return;
            p.getUntapPreventedByPermanentIds().removeIf(sourceId -> {
                Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
                return source == null || !source.isTapped();
            });
        });

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> mayNotUntapPermanents = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(nextActive);
        if (battlefield != null) {
            battlefield.forEach(p -> {
                boolean hasAttachedDoesntUntap = gameQueryService.hasAuraWithEffect(gameData, p, AttachedCreatureDoesntUntapEffect.class);
                boolean hasSelfDoesntUntap = p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof DoesntUntapDuringUntapStepEffect);
                boolean hasMayNotUntap = p.isTapped() && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
                boolean hasUntapLock = !p.getUntapPreventedByPermanentIds().isEmpty();

                if (hasMayNotUntap) {
                    // Present choice to controller later — skip untap for now
                    mayNotUntapPermanents.add(p);
                } else if (!hasAttachedDoesntUntap && !hasSelfDoesntUntap && !hasUntapLock) {
                    p.untap();
                }
                p.setSummoningSick(false);
                p.setLoyaltyAbilityUsedThisTurn(false);
            });
        }

        String untapLog = nextActiveName + " untaps their permanents.";
        gameBroadcastService.logAndBroadcast(gameData, untapLog);
        log.info("Game {} - {} untaps their permanents", gameData.id, nextActiveName);

        // Queue may-not-untap choices for tapped permanents with MayNotUntapDuringUntapStepEffect
        for (Permanent p : mayNotUntapPermanents) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    p.getCard(),
                    nextActive,
                    List.of(new MayNotUntapDuringUntapStepEffect()),
                    "Untap " + p.getCard().getName() + "?"
            ));
        }

        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(nextActive)) return;
            if (!controlsUntapOnEachOtherPlayersStep(gameData, playerId, TurnStep.UNTAP)) return;

            playerBattlefield.forEach(Permanent::untap);

            String playerName = gameData.playerIdToName.get(playerId);
            String seedbornLog = playerName + " untaps their permanents due to Seedborn Muse.";
            gameBroadcastService.logAndBroadcast(gameData, seedbornLog);
            log.info("Game {} - {} untaps permanents due to Seedborn Muse", gameData.id, playerName);
        });

        // Process pending may-not-untap choices before continuing turn
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        String logEntry = "Turn " + gameData.turnNumber + " begins. " + nextActiveName + "'s turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, nextActiveName);
        gameBroadcastService.broadcastGameState(gameData);
    }

    /**
     * Called after all may-not-untap choices have been resolved to finish the turn advance
     * (log the turn start and broadcast game state).
     */
    public void completeTurnAdvance(GameData gameData) {
        String activeName = gameData.playerIdToName.get(gameData.activePlayerId);
        String logEntry = "Turn " + gameData.turnNumber + " begins. " + activeName + "'s turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, activeName);
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
        if (result == CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS) {
            resolveAutoPassCombatTriggers(gameData);
        }
        if (result == CombatResult.ADVANCE_AND_AUTO_PASS || result == CombatResult.AUTO_PASS_ONLY
                || result == CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS) {
            resolveAutoPass(gameData);
        }
    }

    private void resolveAutoPassCombatTriggers(GameData gameData) {
        for (int safety = 0; safety < 100; safety++) {
            if (gameData.stack.isEmpty()) return;
            if (gameData.interaction.isAwaitingInput()) return;
            if (gameData.status == GameStatus.FINISHED) return;

            UUID stackPriorityHolder = gameQueryService.getPriorityPlayerId(gameData);
            if (stackPriorityHolder == null) {
                // Both passed — resolve top of stack
                stackResolutionService.resolveTopOfStack(gameData);
                // After resolution, if user interaction is needed (e.g. multi-permanent choice), stop
                if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                    return;
                }
                gameData.priorityPassedBy.clear();
                continue;
            }

            List<Integer> playable = gameBroadcastService.getPlayableCardIndices(gameData, stackPriorityHolder);
            boolean hasActivatable = hasInstantSpeedActivatedAbility(gameData, stackPriorityHolder);

            if (!playable.isEmpty() || hasActivatable) {
                // Player can respond to the triggered ability — stop and let them
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Auto-pass for this player
            gameData.priorityPassedBy.add(stackPriorityHolder);
        }
    }

    public void applyCleanupResets(GameData gameData) {
        gameHelper.resetEndOfTurnModifiers(gameData);
        auraAttachmentService.returnStolenCreatures(gameData, true);
    }

    public void resolveAutoPass(GameData gameData) {
        // Process any pending spell-target triggers (e.g. Livewire Lash)
        if (!gameData.pendingSpellTargetTriggers.isEmpty()) {
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        }

        // Process any pending discard self-triggers before death triggers
        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }

        // Process any pending targeted attack triggers before death triggers
        if (!gameData.pendingAttackTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextAttackTriggerTarget(gameData);
        }

        // Process any pending targeted death triggers before auto-passing
        if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
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

    private boolean hasInstantSpeedActivatedAbility(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;

        for (Permanent perm : battlefield) {
            for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
                // Skip sorcery-speed and upkeep-only abilities
                if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED
                        || ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP) {
                    continue;
                }

                // Skip mana abilities (all non-cost effects are ManaProducingEffect)
                boolean isManaAbility = ability.getEffects().stream()
                        .allMatch(e -> e instanceof ManaProducingEffect);
                if (isManaAbility) continue;

                // Skip loyalty abilities
                if (ability.getLoyaltyCost() != null) continue;

                // Skip if ability requires tap and permanent is tapped
                if (ability.isRequiresTap() && perm.isTapped()) continue;

                return true;
            }
        }
        return false;
    }
}
