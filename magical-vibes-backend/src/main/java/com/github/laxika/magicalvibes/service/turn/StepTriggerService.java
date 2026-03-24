package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomOpponentPermanentWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlIfSubtypesDealtCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DidntAttackConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromOwnGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NoOtherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NotKickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.UntapUpToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveEggCounterFromExileAndReturnEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final TriggerCollectionService triggerCollectionService;

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

            // If any effect targets a player, group all effects into a player-targeted trigger
            boolean hasPlayerTarget = upkeepEffects.stream().anyMatch(CardEffect::canTargetPlayer);
            if (hasPlayerTarget) {
                int maxPlayerTargets = upkeepEffects.stream()
                        .mapToInt(CardEffect::requiredPlayerTargetCount)
                        .max().orElse(1);
                if (maxPlayerTargets >= 2) {
                    gameData.pendingUpkeepMultiPlayerTargets.add(new PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                } else {
                    gameData.pendingUpkeepPlayerTargets.add(new PermanentChoiceContext.UpkeepPlayerTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                }
                continue;
            }

            for (CardEffect effect : upkeepEffects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                } else if (effect instanceof MayRevealSubtypeFromHandEffect mayReveal) {
                    List<Card> hand = gameData.playerHands.get(activePlayerId);
                    boolean hasSubtype = hand != null && hand.stream()
                            .anyMatch(c -> c.getSubtypes().contains(mayReveal.subtype()));
                    if (hasSubtype) {
                        MayEffect may = new MayEffect(mayReveal.thenEffect(), mayReveal.prompt());
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                    }
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
                } else if (effect instanceof DestroyOneOfTargetsAtRandomEffect) {
                    // Targeted upkeep trigger: targets chosen at trigger time (CR 603.3d).
                    // The Efreet itself is a valid "nonland permanent you control" target,
                    // so this always triggers as long as it's on the battlefield.
                    gameData.pendingCapriciousEfreetTargets.add(new PermanentChoiceContext.CapriciousEfreetOwnTarget(
                            perm.getCard(), activePlayerId, perm.getId()));
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
                } else if (effect instanceof ControllerLifeAtOrBelowThresholdConditionalEffect lifeCheck) {
                    // Intervening-if: only trigger if controller's life total <= threshold
                    int lifeTotal = gameData.playerLifeTotals.getOrDefault(activePlayerId, 20);
                    if (lifeTotal <= lifeCheck.lifeThreshold()) {
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
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: life {} <= {})",
                                gameData.id, perm.getCard().getName(), lifeTotal, lifeCheck.lifeThreshold());
                    }
                } else if (effect instanceof WinGameIfCreaturesInGraveyardEffect winEffect) {
                    // Intervening-if: only trigger if condition is met
                    List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
                    long creatureCount = 0;
                    if (graveyard != null) {
                        creatureCount = graveyard.stream()
                                .filter(c -> c.hasType(CardType.CREATURE))
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
                // Intervening-if: werewolf transform conditions checked at trigger time
                if (effect instanceof NoSpellsCastLastTurnConditionalEffect) {
                    int totalSpells = gameData.spellsCastLastTurn.values().stream()
                            .mapToInt(Integer::intValue).sum();
                    if (totalSpells > 0) continue;
                } else if (effect instanceof TwoOrMoreSpellsCastLastTurnConditionalEffect) {
                    boolean anyPlayerCastTwo = gameData.spellsCastLastTurn.values().stream()
                            .anyMatch(count -> count >= 2);
                    if (!anyPlayerCastTwo) continue;
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(effect)),
                        activePlayerId,
                        perm.getId()
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
            if (!perm.isAttached()) return;

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

        // Check all battlefields for curses with ENCHANTED_PLAYER_UPKEEP_TRIGGERED effects
        // These fire during the enchanted player's upkeep (e.g. Curse of Oblivion, Curse of the Bloody Tome)
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedPlayerUpkeepEffects = perm.getCard().getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED);
            if (enchantedPlayerUpkeepEffects == null || enchantedPlayerUpkeepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            // For curses, attachedTo is the enchanted player's UUID
            UUID enchantedPlayerId = perm.getAttachedTo();
            if (!enchantedPlayerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedPlayerUpkeepEffects) {
                // Bake the enchanted player ID into effects that need it
                CardEffect effectForStack = effect;
                if (effect instanceof ExileCardsFromOwnGraveyardEffect e) {
                    effectForStack = new ExileCardsFromOwnGraveyardEffect(e.count(), enchantedPlayerId);
                } else if (effect instanceof DealDamageToEnchantedPlayerEffect e) {
                    effectForStack = new DealDamageToEnchantedPlayerEffect(e.damage(), enchantedPlayerId);
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(effectForStack)),
                        enchantedPlayerId,
                        perm.getId()
                ));

                String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} enchanted-player upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });

        // Check exiled cards with egg counters owned by the active player (e.g. Darigaaz Reincarnated).
        // "At the beginning of your upkeep, if this card is exiled with an egg counter on it,
        //  remove an egg counter from it. Then if it has no egg counters, return it to the battlefield."
        if (!gameData.exiledCardEggCounters.isEmpty()) {
            List<Card> exiledCards = gameData.getPlayerExiledCards(activePlayerId);
            if (!exiledCards.isEmpty()) {
                for (Card card : new ArrayList<>(exiledCards)) {
                    Integer eggCounters = gameData.exiledCardEggCounters.get(card.getId());
                    if (eggCounters != null && eggCounters > 0) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                activePlayerId,
                                card.getName() + "'s egg counter ability",
                                new ArrayList<>(List.of(new RemoveEggCounterFromExileAndReturnEffect(card.getId())))
                        ));

                        String logEntry = card.getName() + "'s upkeep ability triggers (exiled with egg counters).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} egg counter upkeep trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // Process upkeep multi-player-targeted triggers first (e.g. Axis of Mortality, CR 603.3d)
        if (!gameData.pendingUpkeepMultiPlayerTargets.isEmpty()) {
            processNextUpkeepMultiPlayerTarget(gameData);
            return;
        }

        // Process upkeep player-targeted triggers (mandatory targeting at trigger time, CR 603.3d)
        if (!gameData.pendingUpkeepPlayerTargets.isEmpty()) {
            processNextUpkeepPlayerTarget(gameData);
            return;
        }

        // Process upkeep copy trigger target selection (mandatory targeting at trigger time)
        if (!gameData.pendingUpkeepCopyTargets.isEmpty()) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        if (!gameData.pendingCapriciousEfreetTargets.isEmpty()) {
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Processes the next pending upkeep player-targeted trigger (e.g. Bloodgift Demon).
     * Presents the controller with a player choice; when selected, the trigger is
     * pushed onto the stack with all its effects sharing the chosen target.
     *
     * @param gameData the current game state to modify
     */
    public void processNextUpkeepPlayerTarget(GameData gameData) {
        if (gameData.pendingUpkeepPlayerTargets.isEmpty()) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepPlayerTargetTrigger trigger = gameData.pendingUpkeepPlayerTargets.removeFirst();

        List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose target player.");

        String logEntry = trigger.sourceCard().getName() + "'s upkeep ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} upkeep trigger awaiting player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending upkeep multi-player-targeted trigger (e.g. Axis of Mortality).
     * Presents the controller with a player choice for the first target; when selected,
     * a second target selection is initiated via {@code UpkeepSecondPlayerTargetTrigger}.
     */
    public void processNextUpkeepMultiPlayerTarget(GameData gameData) {
        if (gameData.pendingUpkeepMultiPlayerTargets.isEmpty()) {
            processNextUpkeepPlayerTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger trigger = gameData.pendingUpkeepMultiPlayerTargets.removeFirst();

        List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose first target player.");

        String logEntry = trigger.sourceCard().getName() + "'s upkeep ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} upkeep trigger awaiting first player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the second player target for an upkeep multi-player-targeted trigger.
     * After the second target is selected, the trigger is pushed onto the stack with both targets.
     */
    public void processUpkeepSecondPlayerTarget(GameData gameData, PermanentChoiceContext.UpkeepSecondPlayerTargetTrigger trigger) {
        List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);
        // Cannot target the same player twice
        validPlayerTargets.remove(trigger.firstTargetPlayerId());

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose second target player.");

        log.info("Game {} - {} upkeep trigger awaiting second player target selection", gameData.id, trigger.sourceCard().getName());
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
            // All copy triggers targeted, continue with Capricious Efreet targets then may abilities
            processNextCapriciousEfreetTarget(gameData);
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

    /**
     * Processes the next pending Capricious Efreet upkeep trigger target selection.
     * Step 1: controller chooses one nonland permanent they control.
     */
    public void processNextCapriciousEfreetTarget(GameData gameData) {
        if (gameData.pendingCapriciousEfreetTargets.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.CapriciousEfreetOwnTarget trigger = gameData.pendingCapriciousEfreetTargets.removeFirst();

        // Collect valid own nonland permanents (Efreet itself is a valid target)
        List<UUID> validOwnTargets = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (!p.getCard().hasType(CardType.LAND)) {
                    validOwnTargets.add(p.getId());
                }
            }
        }

        if (validOwnTargets.isEmpty()) {
            // No valid own targets — skip this trigger
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validOwnTargets,
                trigger.sourceCard().getName() + " — Choose a nonland permanent you control.");

        String logEntry = trigger.sourceCard().getName() + "'s upkeep ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} upkeep trigger awaiting own target selection", gameData.id, trigger.sourceCard().getName());
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
        // Saga lore counters: add a lore counter to each Saga the active player controls (MTG Rule 714.3b)
        handleSagaLoreCounters(gameData);

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
     * Adds a lore counter to each Saga the active player controls and triggers
     * the appropriate chapter ability (MTG Rule 714.3b).
     * Called at the beginning of the active player's precombat main phase.
     */
    private void handleSagaLoreCounters(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        // Collect Sagas first to avoid ConcurrentModificationException
        List<Permanent> sagas = battlefield.stream()
                .filter(p -> p.getCard().isSaga())
                .toList();

        for (Permanent saga : sagas) {
            Card card = saga.getCard();
            int newLoreCount = saga.getLoreCounters() + 1;
            saga.setLoreCounters(newLoreCount);

            String counterLog = card.getName() + " gets a lore counter (" + newLoreCount + ").";
            gameBroadcastService.logAndBroadcast(gameData, counterLog);
            log.info("Game {} - {} gets lore counter {}", gameData.id, card.getName(), newLoreCount);

            // Trigger the appropriate chapter ability
            EffectSlot chapterSlot = switch (newLoreCount) {
                case 1 -> EffectSlot.SAGA_CHAPTER_I;
                case 2 -> EffectSlot.SAGA_CHAPTER_II;
                case 3 -> EffectSlot.SAGA_CHAPTER_III;
                default -> null;
            };
            if (chapterSlot == null) continue;

            List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
            if (chapterEffects.isEmpty()) continue;

            String chapterName = switch (newLoreCount) {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                default -> String.valueOf(newLoreCount);
            };

            boolean needsPermanentTarget = chapterEffects.stream().anyMatch(CardEffect::canTargetPermanent);
            boolean needsGraveyardTarget = chapterEffects.stream().anyMatch(CardEffect::canTargetGraveyard);
            if (needsPermanentTarget) {
                gameData.pendingSagaChapterTargets.add(
                        new PermanentChoiceContext.SagaChapterTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName,
                                card.getSagaChapterTargetFilters(chapterSlot)));
                String logEntry = card.getName() + "'s chapter " + chapterName + " ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} chapter {} triggers (awaiting target selection)", gameData.id, card.getName(), chapterName);
            } else if (needsGraveyardTarget) {
                gameData.pendingSagaChapterGraveyardTargets.add(
                        new PermanentChoiceContext.SagaChapterGraveyardTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName));
                String logEntry = card.getName() + "'s chapter " + chapterName + " ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} chapter {} triggers (awaiting graveyard target selection)", gameData.id, card.getName(), chapterName);
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        activePlayerId,
                        card.getName() + "'s chapter " + chapterName + " ability",
                        new ArrayList<>(chapterEffects),
                        null,
                        saga.getId()
                ));

                String logEntry = card.getName() + "'s chapter " + chapterName + " ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
            }
        }

        // Process any queued saga chapter target selections
        if (!gameData.pendingSagaChapterTargets.isEmpty()) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        }
        if (!gameData.pendingSagaChapterGraveyardTargets.isEmpty()) {
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
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

        // Process pending end-step destructions (e.g. Stone Giant)
        if (!gameData.pendingDestroyAtEndStep.isEmpty()) {
            Set<UUID> toDestroy = new HashSet<>(gameData.pendingDestroyAtEndStep);
            gameData.pendingDestroyAtEndStep.clear();
            for (UUID permId : toDestroy) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    if (permanentRemovalService.tryDestroyPermanent(gameData, perm)) {
                        String logEntry = perm.getCard().getName() + " is destroyed at end step.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} destroyed at end step (delayed trigger)", gameData.id, perm.getCard().getName());
                    }
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
                gameData.removeFromExile(card.getId());
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

        // Process delayed +1/+1 counter regrowth triggers (e.g. Protean Hydra)
        // Ruling: "If multiple +1/+1 counters are removed at once, its last ability will trigger that many times."
        // Each removed counter creates a separate delayed trigger that adds 2 +1/+1 counters.
        // The pending map stores countersRemoved * 2 (total counters to add), so we divide by 2
        // to get the number of individual triggers, each adding 2 counters.
        if (!gameData.pendingDelayedPlusOnePlusOneCounters.isEmpty()) {
            Map<UUID, Integer> pendingCounters = new HashMap<>(gameData.pendingDelayedPlusOnePlusOneCounters);
            gameData.pendingDelayedPlusOnePlusOneCounters.clear();
            for (Map.Entry<UUID, Integer> counterEntry : pendingCounters.entrySet()) {
                UUID permanentId = counterEntry.getKey();
                int totalCountersToAdd = counterEntry.getValue();
                Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
                if (perm == null) continue;
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanentId);
                if (controllerId == null) continue;

                int triggerCount = totalCountersToAdd / 2; // each trigger adds 2 counters
                for (int i = 0; i < triggerCount; i++) {
                    PutCountersOnSourceEffect effect = new PutCountersOnSourceEffect(1, 1, 2);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s delayed +1/+1 counter trigger",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                }

                String logEntry = perm.getCard().getName() + "'s delayed trigger — " + triggerCount + " trigger(s), adding " + totalCountersToAdd + " +1/+1 counter(s).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} delayed +1/+1 counter regrowth: {} trigger(s) pushed onto stack", gameData.id, perm.getCard().getName(), triggerCount);
            }
        }

        // Process delayed untap permanents triggers (e.g. Teferi, Hero of Dominaria +1)
        if (!gameData.pendingDelayedUntapPermanents.isEmpty()) {
            List<GameData.DelayedUntapPermanents> pendingUntaps = new ArrayList<>(gameData.pendingDelayedUntapPermanents);
            gameData.pendingDelayedUntapPermanents.clear();
            for (GameData.DelayedUntapPermanents pending : pendingUntaps) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — untap up to " + pending.count() + " permanent(s)",
                        new ArrayList<>(List.of(new UntapUpToControlledPermanentsEffect(pending.count(), pending.filter())))
                ));
                String logEntry = pending.sourceCard().getName() + "'s delayed trigger — untap up to " + pending.count() + " permanent(s).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} delayed untap {} permanent(s) trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName(), pending.count());
            }
        }

        // Process delayed graveyard-to-hand returns (e.g. Tiana, Ship's Caretaker)
        if (!gameData.pendingDelayedGraveyardToHandReturns.isEmpty()) {
            List<GameData.DelayedGraveyardToHandReturn> pendingReturns = new ArrayList<>(gameData.pendingDelayedGraveyardToHandReturns);
            gameData.pendingDelayedGraveyardToHandReturns.clear();
            for (GameData.DelayedGraveyardToHandReturn pending : pendingReturns) {
                List<Card> graveyard = gameData.playerGraveyards.get(pending.ownerId());
                if (graveyard == null) continue;
                Card cardToReturn = null;
                for (Card card : graveyard) {
                    if (card.getId().equals(pending.cardId())) {
                        cardToReturn = card;
                        break;
                    }
                }
                if (cardToReturn != null) {
                    graveyard.remove(cardToReturn);
                    gameData.addCardToHand(pending.ownerId(), cardToReturn);
                    String playerName = gameData.playerIdToName.get(pending.ownerId());
                    String logEntry = cardToReturn.getName() + " returns to " + playerName + "'s hand (delayed trigger).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns to {}'s hand from graveyard (delayed end-step trigger)",
                            gameData.id, cardToReturn.getName(), playerName);
                } else {
                    log.info("Game {} - Delayed graveyard-to-hand return for card {} skipped (no longer in graveyard)",
                            gameData.id, pending.cardId());
                }
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
                    } else if (effect instanceof NotKickedConditionalEffect notKicked) {
                        // Intervening-if: only trigger if the permanent was NOT kicked (CR 603.4)
                        if (perm.isKicked()) {
                            log.info("Game {} - {} end-step trigger skipped (was kicked)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
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
                        log.info("Game {} - {} end-step not-kicked trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof MorbidConditionalEffect morbid) {
                        // Intervening-if: only trigger if morbid condition is met (CR 603.4)
                        if (!gameQueryService.isMorbidMet(gameData)) {
                            log.info("Game {} - {} end-step morbid trigger skipped (no creature died this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        CardEffect wrapped = morbid.wrapped();
                        if (wrapped.canTargetPermanent() || wrapped.canTargetPlayer()) {
                            // Targeting triggered ability — queue for target selection
                            gameData.pendingEndStepTriggerTargets.add(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), playerId, new ArrayList<>(List.of(effect)), perm.getId()));
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
                            log.info("Game {} - {} end-step morbid trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
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
                    if (effect instanceof RaidConditionalEffect raidEffect) {
                        // Intervening-if: only trigger if the controller attacked this turn
                        if (!gameData.playersDeclaredAttackersThisTurn.contains(activePlayerId)) {
                            log.info("Game {} - {} end-step raid trigger skipped (didn't attack this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        CardEffect wrapped = raidEffect.wrapped();
                        if (wrapped instanceof MayEffect may) {
                            gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                        } else if (wrapped.canTargetPermanent() || wrapped.canTargetPlayer()) {
                            // Raid condition met, targeting required — queue for target selection
                            gameData.pendingEndStepTriggerTargets.add(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(wrapped)),
                                    null,
                                    perm.getId()
                            ));

                            String logEntry = perm.getCard().getName() + "'s end step ability triggers.";
                            gameBroadcastService.logAndBroadcast(gameData, logEntry);
                            log.info("Game {} - {} controller end-step raid trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                    } else if (effect instanceof DestroyRandomOpponentPermanentWithCounterEffect destroyRandom) {
                        // Intervening-if: only trigger if enough opponent permanents have the counter
                        int count = 0;
                        for (UUID pid : gameData.orderedPlayerIds) {
                            if (pid.equals(activePlayerId)) continue;
                            List<Permanent> opponentBf = gameData.playerBattlefields.get(pid);
                            if (opponentBf == null) continue;
                            for (Permanent p : opponentBf) {
                                int counterCount = switch (destroyRandom.counterType()) {
                                    case AIM -> p.getAimCounters();
                                    case CHARGE -> p.getChargeCounters();
                                    default -> 0;
                                };
                                if (counterCount > 0) count++;
                            }
                        }
                        if (count < destroyRandom.minRequired()) {
                            log.info("Game {} - {} end-step trigger skipped (only {} permanents with {} counters, need {})",
                                    gameData.id, perm.getCard().getName(), count,
                                    destroyRandom.counterType().name().toLowerCase(), destroyRandom.minRequired());
                            continue;
                        }
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
                    } else if (effect instanceof ControlsPermanentCountConditionalEffect countCheck) {
                        // Intervening-if: only trigger if controller has enough matching permanents
                        List<Permanent> controllerBf = gameData.playerBattlefields.get(activePlayerId);
                        long matchCount = controllerBf == null ? 0 : controllerBf.stream()
                                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, countCheck.filter()))
                                .count();
                        if (matchCount < countCheck.minCount()) {
                            log.info("Game {} - {} end-step trigger skipped (only {} matching permanents, need {})",
                                    gameData.id, perm.getCard().getName(), matchCount, countCheck.minCount());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        String countLogEntry = perm.getCard().getName() + "'s end step ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, countLogEntry);
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof DidntAttackConditionalEffect didntAttack) {
                        // Intervening-if: only trigger if the creature didn't attack this turn
                        if (perm.isAttackedThisTurn()) {
                            log.info("Game {} - {} end-step trigger skipped (attacked this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
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
                    } else if (effect instanceof GainControlIfSubtypesDealtCombatDamageEffect subtypeEffect) {
                        // Intervening-if: check if any opponent was dealt combat damage by enough
                        // creatures of the required subtype this turn
                        boolean conditionMet = false;
                        for (UUID opponentId : gameData.orderedPlayerIds) {
                            if (opponentId.equals(activePlayerId)) continue;
                            int count = 0;
                            for (var dmgEntry : gameData.combatDamageToPlayersThisTurn.entrySet()) {
                                UUID permId = dmgEntry.getKey();
                                if (!dmgEntry.getValue().contains(opponentId)) continue;
                                Set<CardSubtype> subtypes = gameData.combatDamageSourceSubtypesThisTurn
                                        .getOrDefault(permId, Set.of());
                                if (subtypes.contains(subtypeEffect.subtype())
                                        || gameData.combatDamageSourcesWithChangelingThisTurn.contains(permId)) {
                                    count++;
                                }
                            }
                            if (count >= subtypeEffect.threshold()) {
                                conditionMet = true;
                                break;
                            }
                        }
                        if (!conditionMet) {
                            log.info("Game {} - {} end-step trigger skipped (no opponent dealt combat damage by {} or more {}s)",
                                    gameData.id, perm.getCard().getName(), subtypeEffect.threshold(),
                                    subtypeEffect.subtype().getDisplayName());
                            continue;
                        }
                        // Condition met — queue for targeting with GainControlOfTargetPermanentEffect.
                        // The card's targetFilter restricts to nonland opponent permanents.
                        gameData.pendingEndStepTriggerTargets.add(new PermanentChoiceContext.EndStepTriggerTarget(
                                perm.getCard(), activePlayerId,
                                new ArrayList<>(List.of(new GainControlOfTargetPermanentEffect())),
                                perm.getId()));
                    } else if (effect.canTargetPermanent() || effect.canTargetPlayer()) {
                        // Targeting triggered ability — queue for target selection
                        gameData.pendingEndStepTriggerTargets.add(new PermanentChoiceContext.EndStepTriggerTarget(
                                perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect)), perm.getId()));
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

        // Process pending end-step targeted triggers (e.g. Reaper from the Abyss morbid, Voltaic Servant)
        if (!gameData.pendingEndStepTriggerTargets.isEmpty()) {
            processNextEndStepTriggerTarget(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Processes the next pending end-step targeted trigger.
     * Presents the controller with a permanent choice; when selected, the trigger is
     * pushed onto the stack with the chosen target.
     *
     * @param gameData the current game state to modify
     */
    public void processNextEndStepTriggerTarget(GameData gameData) {
        if (gameData.pendingEndStepTriggerTargets.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.EndStepTriggerTarget trigger = gameData.pendingEndStepTriggerTargets.removeFirst();

        // Collect valid targets, respecting the card's target filter if present
        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        FilterContext filterCtx = targetFilter != null
                ? new FilterContext(gameData, trigger.sourceCard().getId(), trigger.controllerId(), null)
                : null;

        List<UUID> validTargets = new ArrayList<>();
        boolean canTargetPlayers = trigger.effects().stream().anyMatch(e -> {
            CardEffect inner = e instanceof ConditionalEffect ce ? ce.wrapped() : e;
            return inner.canTargetPlayer();
        });
        boolean canTargetPermanents = trigger.effects().stream().anyMatch(e -> {
            CardEffect inner = e instanceof ConditionalEffect ce ? ce.wrapped() : e;
            return inner.canTargetPermanent();
        });

        if (canTargetPlayers) {
            if (targetFilter instanceof PlayerPredicateTargetFilter ppf
                    && ppf.predicate() instanceof PlayerRelationPredicate prp
                    && prp.relation() == PlayerRelation.OPPONENT) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    if (!pid.equals(trigger.controllerId())) {
                        validTargets.add(pid);
                    }
                }
            } else {
                validTargets.addAll(gameData.orderedPlayerIds);
            }
        }
        if (canTargetPermanents) {
            // Extract target predicate from effects (e.g. UntapTargetPermanentEffect with artifact restriction)
            PermanentPredicate effectPredicate = trigger.effects().stream()
                    .map(e -> e instanceof ConditionalEffect ce ? ce.wrapped() : e)
                    .filter(e -> e.canTargetPermanent() && e.targetPredicate() != null)
                    .map(CardEffect::targetPredicate)
                    .findFirst().orElse(null);
            FilterContext effectFilterCtx = effectPredicate != null
                    ? new FilterContext(gameData, trigger.sourceCard().getId(), trigger.controllerId(), null)
                    : null;

            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
                        if (!gameQueryService.matchesPermanentPredicate(p, ppf.predicate(), filterCtx)) continue;
                    }
                    if (effectPredicate != null) {
                        if (!gameQueryService.matchesPermanentPredicate(p, effectPredicate, effectFilterCtx)) continue;
                    }
                    validTargets.add(p.getId());
                }
            }
        }

        if (validTargets.isEmpty()) {
            String logEntry = trigger.sourceCard().getName() + "'s end step trigger has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} end-step trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            // Try next pending trigger
            processNextEndStepTriggerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (canTargetPlayers && canTargetPermanents) {
            targetDescription = "any target";
        } else if (canTargetPlayers) {
            targetDescription = "target player";
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        String logEntry = trigger.sourceCard().getName() + "'s end step trigger — choose " + targetDescription + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} end-step trigger awaiting target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Scans the active player's battlefield for permanents with
     * {@code BEGINNING_OF_COMBAT_TRIGGERED} effects and pushes them onto the stack.
     * Only fires for the active player's permanents (CR 507.1: "At the beginning
     * of combat on your turn").
     *
     * @param gameData the current game state to modify
     */
    public void handleBeginningOfCombatTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> combatEffects = perm.getCard().getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED);
            if (combatEffects == null || combatEffects.isEmpty()) continue;

            for (CardEffect effect : combatEffects) {
                // For equipment triggers, only fire if the equipment is attached to a creature
                if (perm.isAttached()) {
                    Permanent equippedCreature = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
                    if (equippedCreature == null || !gameQueryService.isCreature(gameData, equippedCreature)) {
                        continue;
                    }
                }

                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s combat ability",
                            new ArrayList<>(List.of(effect)),
                            (UUID) null,
                            perm.getId()
                    ));

                    String logMsg = perm.getCard().getName() + "'s beginning of combat ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logMsg);
                    log.info("Game {} - {} beginning-of-combat trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }
}
