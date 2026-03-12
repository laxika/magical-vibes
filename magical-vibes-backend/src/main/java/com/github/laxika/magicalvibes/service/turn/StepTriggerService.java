package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NoOtherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Collects and processes triggered abilities that fire at the beginning of
 * specific turn steps: upkeep, draw, precombat main, and end step.
 *
 * <p>Extracted from {@code TurnProgressionService} to isolate the trigger-
 * scanning logic.  For each step the service iterates the relevant
 * {@link EffectSlot}s on permanents (and graveyards, for upkeep triggers),
 * pushes {@link StackEntry}s onto the stack, and queues
 * {@link MayEffect}/{@link MayPayManaEffect} choices as needed.
 *
 * <p>Also handles the Chancellor cycle's opening-hand reveal triggers on
 * the first upkeep, and upkeep copy-trigger target selection (CR 603.3d).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StepTriggerService {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;

    /**
     * Scans battlefields, graveyards, and (on turn 1) hands for upkeep-triggered
     * abilities and pushes them onto the stack or queues may-ability prompts.
     *
     * <p>Handles slots: {@code UPKEEP_TRIGGERED}, {@code GRAVEYARD_UPKEEP_TRIGGERED},
     * {@code EACH_UPKEEP_TRIGGERED}, {@code OPPONENT_UPKEEP_TRIGGERED},
     * {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED}, and
     * {@code ON_OPENING_HAND_REVEAL} (Chancellor cycle, turn 1 only).
     *
     * @param gameData the current game state to modify
     */
    public void handleUpkeepTriggers(GameData gameData) {
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

        // Check all battlefields for auras with ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED effects
        // These fire during the enchanted permanent's controller's upkeep (e.g. Numbing Dose)
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedControllerUpkeepEffects = perm.getCard().getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED);
            if (enchantedControllerUpkeepEffects == null || enchantedControllerUpkeepEffects.isEmpty()) return;
            if (perm.getAttachedTo() == null) return;

            UUID enchantedPermanentControllerId = gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedControllerUpkeepEffects) {
                // Bake the enchanted permanent's controller into effects that need it
                CardEffect effectForStack = effect;
                if (effect instanceof EnchantedCreatureControllerLosesLifeEffect e) {
                    effectForStack = new EnchantedCreatureControllerLosesLifeEffect(e.amount(), enchantedPermanentControllerId);
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(effectForStack)),
                        (UUID) null,
                        perm.getId()
                ));

                String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} enchanted-permanent-controller upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });

        // Process upkeep copy trigger target selection first (mandatory targeting at trigger time)
        if (!gameData.pendingUpkeepCopyTargets.isEmpty()) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Processes the next pending upkeep copy-trigger target selection
     * (e.g. Clone Shell).  If no targets remain, continues to may-ability
     * processing.
     *
     * @param gameData the current game state to modify
     */
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
                    // Leyline effects are handled during the pregame procedure
                    // (MulliganService.startGame), not during the first upkeep.
                    if (effect instanceof MayEffect may
                            && may.wrapped() instanceof LeylineStartOnBattlefieldEffect) {
                        continue;
                    }
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

    /**
     * Executes the draw step: the active player draws a card (rule 504.1),
     * unless it is turn 1 for the starting player (rule 103.7a).
     * Then scans for {@code DRAW_TRIGGERED} and {@code EACH_DRAW_TRIGGERED}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleDrawStep(GameData gameData) {
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

    /**
     * Fires Chancellor-style delayed mana triggers at the beginning of the
     * revealing player's first precombat main phase.
     *
     * @param gameData the current game state to modify
     */
    public void handlePrecombatMainTriggers(GameData gameData) {
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

    /**
     * Processes end-step triggers: exiles pending tokens (e.g. Mimic Vat),
     * returns pending exile-return cards, then scans battlefields for
     * {@code END_STEP_TRIGGERED} and {@code CONTROLLER_END_STEP_TRIGGERED}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleEndStepTriggers(GameData gameData) {
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
                if (pending.returnTapped()) {
                    perm.tap();
                }
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

        // CONTROLLER_END_STEP_TRIGGERED: only fires for the active player's permanents
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                List<CardEffect> controllerEndStepEffects = perm.getCard().getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED);
                if (controllerEndStepEffects == null || controllerEndStepEffects.isEmpty()) continue;

                for (CardEffect effect : controllerEndStepEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        String logEntry = perm.getCard().getName() + "'s end step ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }
}
