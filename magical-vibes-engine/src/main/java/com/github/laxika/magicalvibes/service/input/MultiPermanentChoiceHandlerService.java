package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingCapriciousEfreetState;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles multi-permanent choice inputs where the player selects
 * zero or more permanents from a list.
 *
 * <p>Covers exile-damaged-player-permanent, sacrifice-self-to-destroy,
 * sacrifice attacking creatures, combat damage bounce, awakening counter
 * placement, proliferate, and tap-subtype-boost (e.g. Myr Battlesphere).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiPermanentChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final TurnProgressionService turnProgressionService;
    private final com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService battlefieldEntryService;
    private final DestructionSupport destructionSupport;
    private final CreatureControlService creatureControlService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AnimationSupport animationSupport;
    private final LifeSupport lifeSupport;
    private final DamagePreventionService damagePreventionService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport permanentControlSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport tapUntapSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport librarySearchSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.KillingWaveEffectHandler killingWaveEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EquipoiseSupport equipoiseSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx
            .PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffectHandler
            tappedLandSacrificeDamageIfSubtypeHandler;

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class) == null) {
            throw new IllegalStateException("Not awaiting multi-permanent choice");
        }
        PendingInteraction.MultiPermanentChoice multiPermanentChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        if (multiPermanentChoice == null || !player.getId().equals(multiPermanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        List<UUID> validIds = multiPermanentChoice.validIds();
        int maxCount = multiPermanentChoice.maxCount();

        if (permanentIds == null) {
            permanentIds = List.of();
        }

        // Validate before touching interaction state: a rejected answer must leave the prompt
        // standing so the player can answer again. Clearing first and then throwing destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry, wedging
        // the game (and with it deferPlayerLossCheck) on nothing worse than a stale client answer
        // — a permanent that died between prompt and answer is an ordinary race.
        if (permanentIds.size() > maxCount) {
            throw new IllegalStateException("Too many permanents selected: " + permanentIds.size() + " > " + maxCount);
        }

        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }

        for (UUID permId : permanentIds) {
            if (!validIds.contains(permId)) {
                throw new IllegalStateException("Invalid permanent: " + permId);
            }
        }

        MultiPermanentChoiceContext context = multiPermanentChoice.context();
        if (context instanceof MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource powerCtx
                && !permanentIds.isEmpty()
                && totalEffectivePower(gameData, permanentIds) < powerCtx.requiredPower()) {
            // Not a partial cost: the set either reaches the threshold or is not a legal choice,
            // so leave the prompt standing rather than sacrificing creatures for nothing.
            throw new IllegalStateException("Selected creatures have total power below "
                    + powerCtx.requiredPower());
        }

        gameData.interaction.clearAwaitingInput();

        if (context instanceof MultiPermanentChoiceContext.ExileDamagedPlayerControls) {
            handleExileDamagedPlayerControlsPermanent(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyDamagedPlayerControls ctx) {
            handleDestroyDamagedPlayerControlsPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.UntapChosenPermanent ctx) {
            handleUntapChosenPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapChosenPermanent ctx) {
            handleTapChosenPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeDamagedPlayerControls ctx) {
            handleSacrificeDamagedPlayerControlsPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeSelfToDestroy ctx) {
            handleSacrificeSelfToDestroy(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.GainControlOfPermanentAndAssignNoCombatDamage ctx) {
            handleGainControlOfPermanentAndAssignNoCombatDamage(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TransformAndAttach ctx) {
            handleTransformAndAttach(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeAttackingCreatures) {
            handleSacrificeAttackingCreature(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.ExileAttackingCreatures) {
            handleExileAttackingCreatures(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary ctx) {
            handlePutAttackingCreaturesOnLibrary(gameData, permanentIds, multiPermanentChoice.validIds(), ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyCreaturesOpponentControls ctx) {
            handleDestroyCreaturesOpponentControls(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapChosenPermanents ctx) {
            handleTapChosenPermanents(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ReturnTargetPermanentsToHand) {
            handleReturnTargetPermanentsToHand(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.CombatDamageBounce ctx) {
            handleCombatDamageBounce(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.AimCounterPlacement) {
            handleAimCounterPlacement(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.OwnPermanentCounterPlacement ctx) {
            handleOwnPermanentCounterPlacement(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.AwakeningCounterPlacement) {
            handleAwakeningCounterPlacement(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.Proliferate ctx) {
            handleProliferate(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapSubtypeBoost ctx) {
            handleTapSubtypeBoost(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyRestChoice ctx) {
            handleDestroyRestChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedSacrifice ctx) {
            handleForcedSacrifice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedSacrificeThenDamageIfSubtype ctx) {
            handleForcedSacrificeThenDamageIfSubtype(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedDestroy ctx) {
            handleForcedDestroy(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedReturnToHand ctx) {
            handleForcedReturnToHand(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseCreatureRestCantBlock ctx) {
            handleChooseCreatureRestCantBlock(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseCreaturesToAttackNextTurn ctx) {
            handleChooseCreaturesToAttackNextTurn(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapCreaturesGainLife ctx) {
            handleTapCreaturesGainLife(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapCreaturesCreateTokens ctx) {
            handleTapCreaturesCreateTokens(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeLandsSearchLandsToBattlefieldTapped) {
            handleSacrificeLandsSearchLandsToBattlefieldTapped(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsDrawPerSacrificed) {
            handleSacrificePermanentsDrawPerSacrificed(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource ctx) {
            handleSacrificeCreaturesWithTotalPowerOrSacrificeSource(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseFivePermanentsSearchSameNameToBattlefieldTapped) {
            handleChooseFivePermanentsSearchSameName(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.DevourSacrifice ctx) {
            handleDevourSacrifice(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.PayManaPerCreatureUntap ctx) {
            handlePayManaPerCreatureUntap(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.StaticOrbUntap ctx) {
            handleStaticOrbUntap(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ExileTetraviteTokensPutCountersOnSource ctx) {
            handleExileTetraviteTokensPutCountersOnSource(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.KillingWaveKeep ctx) {
            handleKillingWaveKeep(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.EquipoisePhaseOut ctx) {
            equipoiseSupport.handleChosen(gameData, permanentIds, ctx);
        } else if (gameData.hasPendingInteraction(PendingCapriciousEfreetState.class)) {
            handleCapriciousEfreetOpponentTargets(gameData, permanentIds);
        } else if (gameData.hasPendingInteraction(PendingPileSeparation.class)) {
            handlePileSeparation(gameData, permanentIds);
        } else {
            throw new IllegalStateException("No pending multi-permanent choice context");
        }
    }

    private void handleSacrificeSelfToDestroy(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                              MultiPermanentChoiceContext.SacrificeSelfToDestroy context) {
        UUID sourcePermId = context.sourcePermanentId();

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to sacrifice.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
            if (source != null) {
                if (permanentRemovalService.removePermanentToGraveyard(gameData, source)) {
                    triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, source.getCard());
                    gameLogService.append(gameData, GameLog.isSacrificed(source.getCard()));
                    log.info("Game {} - {} sacrificed for combat damage trigger", gameData.id, source.getCard().getName());

                    UUID chosenPermId = permanentIds.getFirst();
                    Permanent target = gameQueryService.findPermanentById(gameData, chosenPermId);
                    if (target != null) {
                        if (permanentRemovalService.tryDestroyPermanent(gameData, target, context.cannotBeRegenerated())) {
                            gameLogService.append(gameData, GameLog.isDestroyed(target.getCard()));
                            log.info("Game {} - {} destroyed by sacrifice trigger", gameData.id, target.getCard().getName());
                        }
                    }
                }

                permanentRemovalService.removeOrphanedAuras(gameData);
            } else {
                String logEntry = "Source creature no longer exists — sacrifice trigger fizzles.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleGainControlOfPermanentAndAssignNoCombatDamage(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                                     MultiPermanentChoiceContext.GainControlOfPermanentAndAssignNoCombatDamage context) {
        UUID sourcePermId = context.sourcePermanentId();
        ControlDuration duration = context.duration();

        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(playerId) + " chooses not to gain control of a " + context.choiceNoun() + "."));
        } else {
            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
            UUID sourceController = source != null ? gameQueryService.findPermanentController(gameData, sourcePermId) : null;
            Permanent stolen = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());

            // Per ruling: a source-linked duration only takes control if you still control the
            // source; the "assigns no combat damage" rider applies only when one is actually taken.
            boolean sourceOk = !duration.isSourceLinked() || (source != null && playerId.equals(sourceController));
            if (stolen != null && sourceOk) {
                creatureControlService.applyControlEffect(gameData, playerId, stolen,
                        new GainControlOfTargetEffect(duration), duration.toEffectDuration(),
                        duration.isSourceLinked() ? sourcePermId : null,
                        source != null ? source.getCard().getName() : stolen.getCard().getName());
                if (source != null) {
                    gameData.creaturesPreventedFromDealingCombatDamage.add(sourcePermId);
                    gameLogService.append(gameData,
                            GameLog.cardThen(source.getCard(), " assigns no combat damage this turn."));
                }
                log.info("Game {} - {} gains control of {} and assigns no combat damage",
                        gameData.id, gameData.playerIdToName.get(playerId), stolen.getCard().getName());
            } else {
                gameLogService.append(gameData,
                        GameLog.text("The ability has no effect (source no longer controlled)."));
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTransformAndAttach(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.TransformAndAttach context) {
        UUID sourcePermId = context.sourcePermanentId();

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to attach.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            animationSupport.completeTransformAndAttach(
                    gameData, playerId, sourcePermId, permanentIds.getFirst());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleExileDamagedPlayerControlsPermanent(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to exile a permanent.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            UUID chosenPermId = permanentIds.getFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, chosenPermId);
            if (target != null) {
                permanentRemovalService.removePermanentToExile(gameData, target);
                gameLogService.append(gameData, GameLog.isExiled(target.getCard()));
                log.info("Game {} - {} exiled by combat damage trigger", gameData.id, target.getCard().getName());

                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDestroyDamagedPlayerControlsPermanent(GameData gameData, List<UUID> permanentIds,
                                                             MultiPermanentChoiceContext.DestroyDamagedPlayerControls context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                destructionSupport.tryDestroyAndLog(gameData, target, context.sourceName());
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleUntapChosenPermanent(GameData gameData, List<UUID> permanentIds,
                                            MultiPermanentChoiceContext.UntapChosenPermanent context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                tapUntapSupport.untapPermanent(gameData, target);
                gameLogService.append(gameData,
                        GameLog.builder().text(context.sourceName() + " untaps ").card(target.getCard()).text(".").build());
                log.info("Game {} - {} untaps {}", gameData.id, context.sourceName(), target.getCard().getName());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapChosenPermanent(GameData gameData, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.TapChosenPermanent context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                tapUntapSupport.tapPermanent(gameData, target);
                gameLogService.append(gameData,
                        GameLog.builder().text(context.sourceName() + " taps ").card(target.getCard()).text(".").build());
                log.info("Game {} - {} taps {}", gameData.id, context.sourceName(), target.getCard().getName());

                if (context.preventUntapWhileSourceTapped() && context.sourcePermanentId() != null) {
                    target.getUntapPreventedByPermanentIds().add(context.sourcePermanentId());
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                            " won't untap as long as " + context.sourceName() + " remains tapped."));
                }
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificeDamagedPlayerControlsPermanent(GameData gameData, List<UUID> permanentIds,
                                                               MultiPermanentChoiceContext.SacrificeDamagedPlayerControls context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
                String ownerName = controllerId != null ? gameData.playerIdToName.get(controllerId) : "Unknown";
                if (permanentRemovalService.removePermanentToGraveyard(gameData, target)) {
                    if (controllerId != null) {
                        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, controllerId, target.getCard());
                    }
                    gameLogService.append(gameData, GameLog.playerSacrifices(ownerName, target.getCard()));
                    log.info("Game {} - {} sacrificed by {}", gameData.id, target.getCard().getName(), context.sourceName());
                }
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificeAttackingCreature(GameData gameData, List<UUID> permanentIds) {
        for (UUID permId : permanentIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, permId);
            if (creature != null) {
                UUID ownerId = null;
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> bf = gameData.playerBattlefields.get(pid);
                    if (bf != null && bf.contains(creature)) {
                        ownerId = pid;
                        break;
                    }
                }
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                String ownerName = ownerId != null ? gameData.playerIdToName.get(ownerId) : "Unknown";
                gameLogService.append(gameData, GameLog.playerSacrifices(ownerName, creature.getCard()));
                log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, creature.getCard().getName());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleExileAttackingCreatures(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to exile any attacking creatures."));
        } else {
            List<Card> exiledCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, permId);
                if (creature != null) {
                    permanentRemovalService.removePermanentToExile(gameData, creature);
                    exiledCards.add(creature.getCard());
                }
            }
            if (!exiledCards.isEmpty()) {
                permanentRemovalService.removeOrphanedAuras(gameData);
                gameLogService.append(gameData,
                        appendCards(GameLog.builder(), exiledCards)
                                .text((exiledCards.size() == 1 ? " is" : " are") + " exiled.").build());
                log.info("Game {} - {} exiles {} attacking creatures", gameData.id,
                        gameData.playerIdToName.get(playerId), exiledCards.size());
            }
        }

        // Resume resolving remaining effects on the same ability (e.g. the cycling draw)
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handlePutAttackingCreaturesOnLibrary(GameData gameData, List<UUID> topCreatureIds,
            List<UUID> currentOwnerCreatureIds,
            MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary context) {
        List<UUID> topIds = new ArrayList<>(context.topCreatureIds());
        topIds.addAll(topCreatureIds);
        Set<UUID> selectedTopIds = new HashSet<>(topCreatureIds);
        List<UUID> bottomIds = new ArrayList<>(context.bottomCreatureIds());
        for (UUID creatureId : currentOwnerCreatureIds) {
            if (!selectedTopIds.contains(creatureId)) {
                bottomIds.add(creatureId);
            }
        }

        continuePutAttackingCreaturesOnLibrary(gameData, context.remainingCreatureIds(), topIds,
                bottomIds, context.sourceCardName());
    }

    private void continuePutAttackingCreaturesOnLibrary(GameData gameData, List<UUID> pendingIds,
            List<UUID> topIds, List<UUID> bottomIds, String sourceCardName) {
        List<UUID> remainingIds = pendingIds.stream()
                .filter(creatureId -> gameQueryService.findPermanentById(gameData, creatureId) != null)
                .toList();
        if (!remainingIds.isEmpty()) {
            Permanent first = gameQueryService.findPermanentById(gameData, remainingIds.getFirst());
            UUID ownerId = ownerId(gameData, first);
            List<UUID> ownerCreatureIds = new ArrayList<>();
            List<UUID> laterCreatureIds = new ArrayList<>();
            for (UUID creatureId : remainingIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
                if (ownerId.equals(ownerId(gameData, creature))) {
                    ownerCreatureIds.add(creatureId);
                } else {
                    laterCreatureIds.add(creatureId);
                }
            }

            MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary nextContext =
                    new MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary(laterCreatureIds,
                            topIds, bottomIds, sourceCardName);
            playerInputService.beginMultiPermanentChoice(gameData, ownerId, ownerCreatureIds,
                    ownerCreatureIds.size(), nextContext,
                    sourceCardName + " — Choose attacking creatures to put on top of their owners' libraries. "
                            + "The rest go on the bottom.");
            return;
        }

        for (UUID creatureId : topIds.reversed()) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature != null && permanentRemovalService.removePermanentToLibraryTop(gameData, creature)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(creature.getCard(), " is put on top of its owner's library."));
            }
        }
        for (UUID creatureId : bottomIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature != null && permanentRemovalService.removePermanentToLibraryBottom(gameData, creature)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(creature.getCard(), " is put on the bottom of its owner's library."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private UUID ownerId(GameData gameData, Permanent permanent) {
        UUID ownerId = permanent.getCard().getOwnerId();
        if (ownerId == null) {
            ownerId = gameData.defaultControllerOf(permanent.getId());
        }
        if (ownerId == null) {
            ownerId = gameData.currentlyResolvingControllerId;
        }
        return ownerId;
    }

    private void handleDestroyCreaturesOpponentControls(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                        MultiPermanentChoiceContext.DestroyCreaturesOpponentControls ctx) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to destroy any creatures."));
        } else {
            for (UUID permId : permanentIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, permId);
                if (creature != null) {
                    destructionSupport.tryDestroyAndLog(gameData, creature, ctx.sourceName(), ctx.cannotBeRegenerated());
                }
            }
        }

        // Resume resolving remaining effects on the same entry (the chooser's "draws up to three cards")
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapChosenPermanents(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                           MultiPermanentChoiceContext.TapChosenPermanents ctx) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to tap any permanents."));
        } else {
            int tapped = 0;
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null && tapUntapSupport.tapPermanent(gameData, perm)) {
                    tapped++;
                }
            }
            gameLogService.append(gameData, GameLog.text(
                    ctx.sourceName() + " taps " + tapped + " permanent(s)."));
            log.info("Game {} - {} taps {} chosen permanent(s)", gameData.id, ctx.sourceName(), tapped);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleReturnTargetPermanentsToHand(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses not to return any permanents."));
        } else {
            List<Card> bouncedCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null && permanentRemovalService.removePermanentToHand(gameData, perm)) {
                    bouncedCards.add(perm.getCard());
                }
            }
            if (!bouncedCards.isEmpty()) {
                permanentRemovalService.removeOrphanedAuras(gameData);
                gameLogService.append(gameData,
                        appendCards(GameLog.builder(), bouncedCards)
                                .text((bouncedCards.size() == 1 ? " is" : " are")
                                        + " returned to their owners' hands.").build());
                log.info("Game {} - {} returns {} permanents to hand", gameData.id,
                        gameData.playerIdToName.get(playerId), bouncedCards.size());
            }
        }

        // Resume resolving remaining effects on the same ability (e.g. the cycling draw)
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedSacrifice(GameData gameData, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.ForcedSacrifice context) {
        UUID sacrificingPlayerId = context.sacrificingPlayerId();

        boolean simultaneousFlow = !context.accumulatedSacrificeIds().isEmpty()
                || !context.remainingChoosers().isEmpty();

        if (simultaneousFlow) {
            // "Each player sacrifices" flow — defer actual sacrifice until all players have chosen.
            // Per CR 101.4: all chosen permanents are sacrificed at the same time.
            List<UUID> allIds = new ArrayList<>(context.accumulatedSacrificeIds());
            allIds.addAll(permanentIds);

            if (!context.remainingChoosers().isEmpty()) {
                // More players still need to choose — prompt the next one
                destructionSupport.beginNextForcedSacrificeFromQueue(gameData,
                        context.remainingChoosers(), allIds);
                return;
            }

            // All players have chosen — sacrifice all simultaneously
            for (UUID permId : allIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                    String ownerName = controllerId != null ? gameData.playerIdToName.get(controllerId) : "Unknown";
                    permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                    gameLogService.append(gameData, GameLog.playerSacrifices(ownerName, perm.getCard()));
                    log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, perm.getCard().getName());
                }
            }
        } else {
            // Direct forced sacrifice (e.g. Phyrexian Obliterator) — sacrifice immediately
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    String ownerName = sacrificingPlayerId != null ? gameData.playerIdToName.get(sacrificingPlayerId) : "Unknown";
                    permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                    gameLogService.append(gameData, GameLog.playerSacrifices(ownerName, perm.getCard()));
                    log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, perm.getCard().getName());
                }
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        // Follow the same pattern as proliferate completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedSacrificeThenDamageIfSubtype(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.ForcedSacrificeThenDamageIfSubtype context) {
        List<UUID> allIds = new ArrayList<>(context.accumulatedSacrificeIds());
        allIds.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            tappedLandSacrificeDamageIfSubtypeHandler.beginNextChooser(gameData,
                    context.remainingChoosers(), allIds, context.subtype(), context.damageAmount(),
                    context.damageEntry());
            return;
        }

        tappedLandSacrificeDamageIfSubtypeHandler.sacrificeThenDamageIfSubtype(gameData,
                context.damageEntry(), allIds, context.subtype(), context.damageAmount());

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedDestroy(GameData gameData, List<UUID> permanentIds,
                                     MultiPermanentChoiceContext.ForcedDestroy context) {
        // Chosen permanents are destroyed simultaneously (regeneration/indestructible apply).
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.tryDestroyAndLog(gameData, perm, context.sourceName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleForcedReturnToHand(GameData gameData, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.ForcedReturnToHand context) {
        List<Card> bouncedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && permanentRemovalService.removePermanentToHand(gameData, perm)) {
                bouncedCards.add(perm.getCard());
            }
        }
        if (!bouncedCards.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            gameLogService.append(gameData,
                    appendCards(GameLog.builder(), bouncedCards)
                            .text((bouncedCards.size() == 1 ? " is" : " are")
                                    + " returned to their owners' hands.").build());
            log.info("Game {} - {} returns {} permanents to hand", gameData.id,
                    gameData.playerIdToName.get(context.returningPlayerId()), bouncedCards.size());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificeLandsSearchLandsToBattlefieldTapped(GameData gameData, UUID playerId,
                                                                    List<UUID> permanentIds) {
        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Search the library for up to that many land cards, put them onto the battlefield tapped.
        if (sacrificed > 0 && !librarySearchSupport.isSearchPrevented(gameData, playerId)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            } else {
                List<Card> lands = deck.stream()
                        .filter(card -> card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                        .toList();
                if (lands.isEmpty()) {
                    com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                    gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no land cards. Library is shuffled."));
                } else {
                    String prompt = "Search your library for up to " + sacrificed + " land card"
                            + (sacrificed != 1 ? "s" : "")
                            + " and put them onto the battlefield tapped (" + sacrificed + " remaining).";
                    librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId,
                            com.github.laxika.magicalvibes.model.LibrarySearchParams.builder(playerId, new ArrayList<>(lands))
                                    .remainingCount(sacrificed)
                                    .canFailToFind(true)
                                    .destination(com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_TAPPED)
                                    .build(), prompt, true);
                    // Library search interaction is now active; it resumes effect resolution on completion.
                    return;
                }
            }
        }

        // No search begun — follow standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleSacrificePermanentsDrawPerSacrificed(GameData gameData, UUID playerId,
                                                            List<UUID> permanentIds) {
        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (sacrificed > 0) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, sacrificed);
        } else {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " sacrifices no permanents."));
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private int totalEffectivePower(GameData gameData, List<UUID> permanentIds) {
        int total = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                total += Math.max(0, gameQueryService.getEffectivePower(gameData, perm));
            }
        }
        return total;
    }

    /**
     * Phyrexian Dreadnought: an empty selection means the controller declined, so the source is
     * sacrificed. Otherwise the chosen creatures are sacrificed (their total power was validated
     * against the threshold before the interaction was cleared) and the source survives.
     */
    private void handleSacrificeCreaturesWithTotalPowerOrSacrificeSource(GameData gameData, UUID playerId,
            List<UUID> permanentIds,
            MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource context) {
        if (permanentIds.isEmpty()) {
            Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
            if (source != null) {
                destructionSupport.sacrificeAndLog(gameData, source, playerId);
            }
        } else {
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                }
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleChooseFivePermanentsSearchSameName(GameData gameData, UUID playerId,
                                                          List<UUID> permanentIds) {
        List<String> names = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                names.add(perm.getCard().getName());
            }
        }

        if (names.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " chooses no permanents."));
        } else if (librarySearchSupport.startNextSameNamePick(gameData, playerId,
                LibrarySearchFollowUp.sameNamePicks(names, false,
                        com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_TAPPED))) {
            // A same-name search is now active; it resumes effect resolution on completion.
            return;
        } else if (!librarySearchSupport.isSearchPrevented(gameData, playerId)) {
            // The controller searched but found no matching cards — shuffle once.
            com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " finds no matching cards. Library is shuffled."));
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDestroyRestChoice(GameData gameData, List<UUID> permanentIds,
                                         MultiPermanentChoiceContext.DestroyRestChoice context) {
        destructionSupport.completeDestroyRestChoice(gameData, permanentIds, context);

        // If we're still awaiting input (next player's choice), return
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Destruction is complete — follow standard completion: SBA → may abilities → resume effects
        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleChooseCreatureRestCantBlock(GameData gameData, List<UUID> permanentIds,
                                                   MultiPermanentChoiceContext.ChooseCreatureRestCantBlock context) {
        UUID targetPlayerId = context.targetPlayerId();
        UUID keptId = permanentIds.isEmpty() ? null : permanentIds.getFirst();

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        int count = 0;
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !perm.getId().equals(keptId)) {
                    perm.setCantBlockThisTurn(true);
                    count++;
                }
            }
        }

        if (count > 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text("Other creatures controlled by " + playerName + " can't block this turn."));
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleChooseCreaturesToAttackNextTurn(GameData gameData, List<UUID> permanentIds,
                                                       MultiPermanentChoiceContext.ChooseCreaturesToAttackNextTurn context) {
        UUID targetPlayerId = context.targetPlayerId();
        gameData.chosenAttackersNextTurn.put(targetPlayerId, Set.copyOf(permanentIds));

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName + " chooses " + permanentIds.size()
                + " creature(s) that must attack during their next turn; other creatures can't attack."));

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleCombatDamageBounce(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.CombatDamageBounce context) {
        UUID targetPlayerId = context.targetPlayerId();

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to return any permanents.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            List<Permanent> targetBattlefield = gameData.playerBattlefields.get(targetPlayerId);
            List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
            List<String> bouncedNames = new ArrayList<>();

            for (UUID permId : permanentIds) {
                Permanent toReturn = null;
                for (Permanent p : targetBattlefield) {
                    if (p.getId().equals(permId)) {
                        toReturn = p;
                        break;
                    }
                }
                if (toReturn != null) {
                    targetBattlefield.remove(toReturn);
                    targetHand.add(toReturn.getCard());
                    bouncedNames.add(toReturn.getCard().getName());
                }
            }

            if (!bouncedNames.isEmpty()) {
                permanentRemovalService.removeOrphanedAuras(gameData);
                String logEntry = String.join(", ", bouncedNames) + (bouncedNames.size() == 1 ? " is" : " are") + " returned to " + gameData.playerIdToName.get(targetPlayerId) + "'s hand.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} bounced {} permanents", gameData.id, gameData.playerIdToName.get(playerId), bouncedNames.size());
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.advanceStep(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleAwakeningCounterPlacement(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to put awakening counters on any lands.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            List<Card> awakenedCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    perm.setCounterCount(CounterType.AWAKENING, perm.getCounterCount(CounterType.AWAKENING) + 1);
                    awakenedCards.add(perm.getCard());
                }
            }

            if (!awakenedCards.isEmpty()) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder(), awakenedCards)
                                .text((awakenedCards.size() == 1 ? " receives" : " receive")
                                        + " an awakening counter and "
                                        + (awakenedCards.size() == 1 ? "becomes an" : "become")
                                        + " 8/8 green Elemental creature"
                                        + (awakenedCards.size() == 1 ? "." : "s."))
                                .build());
                log.info("Game {} - Awakening counters placed on {} lands", gameData.id, awakenedCards.size());
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.advanceStep(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleAimCounterPlacement(GameData gameData, List<UUID> permanentIds) {
        if (gameData.pendingEffectResolutionEntry != null) {
            permanentCounterSupport.placeCountersOnPermanents(gameData,
                    gameData.pendingEffectResolutionEntry, permanentIds, CounterType.AIM);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleOwnPermanentCounterPlacement(GameData gameData, List<UUID> permanentIds,
                                                    MultiPermanentChoiceContext.OwnPermanentCounterPlacement context) {
        CounterType counterType = context.counterType();
        int count = context.count();

        if (!permanentIds.isEmpty() && gameData.pendingEffectResolutionEntry != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                permanentCounterSupport.placeCounterOnPermanent(gameData,
                        gameData.pendingEffectResolutionEntry, target, counterType, count);
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleProliferate(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                   MultiPermanentChoiceContext.Proliferate context) {
        int remainingProliferates = context.remainingCount() - 1;

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to proliferate any permanents.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            List<Card> proliferatedCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    if (!gameQueryService.cantHaveCounters(gameData, perm)) {
                        if (perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
                            int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, perm, 1);
                            if (placed > 0) {
                                perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                            }
                        }
                        if (perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                                && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, perm)
                                && gameQueryService.reduceMinusOneMinusOneCounters(gameData, perm, 1) > 0) {
                            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + 1);
                            // The proliferating player is the one putting the counter (Nest of Scarabs).
                            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, perm, 1, playerId);
                        }
                        if (perm.getCounterCount(CounterType.LOYALTY) > 0) {
                            perm.setCounterCount(CounterType.LOYALTY, perm.getCounterCount(CounterType.LOYALTY) + 1);
                        }
                        if (perm.getCounterCount(CounterType.SLIME) > 0) {
                            perm.setCounterCount(CounterType.SLIME, perm.getCounterCount(CounterType.SLIME) + 1);
                        }
                        if (perm.getCounterCount(CounterType.AWAKENING) > 0) {
                            perm.setCounterCount(CounterType.AWAKENING, perm.getCounterCount(CounterType.AWAKENING) + 1);
                        }
                        if (perm.getCounterCount(CounterType.AIM) > 0) {
                            perm.setCounterCount(CounterType.AIM, perm.getCounterCount(CounterType.AIM) + 1);
                        }
                    }
                    proliferatedCards.add(perm.getCard());
                }
            }

            if (!proliferatedCards.isEmpty()) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text("Proliferate adds counters to "), proliferatedCards)
                                .text(".").build());
                log.info("Game {} - Proliferated {} permanents", gameData.id, proliferatedCards.size());
            }
        }

        // More proliferates remaining (e.g. "proliferate, then proliferate again")
        // Per MTG Rule 704.3, SBA are not checked during ability resolution,
        // so defer SBA until all proliferates are done.
        if (remainingProliferates > 0) {
            List<UUID> eligiblePermanentIds = new ArrayList<>();
            gameData.forEachPermanent((pid, p) -> {
                if (p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0
                        || p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                        || p.getCounterCount(CounterType.LOYALTY) > 0
                        || p.getCounterCount(CounterType.SLIME) > 0
                        || p.getCounterCount(CounterType.AWAKENING) > 0
                        || p.getCounterCount(CounterType.AIM) > 0) {
                    eligiblePermanentIds.add(p.getId());
                }
            });
            if (eligiblePermanentIds.isEmpty()) {
                String logEntry = "Proliferate: no permanents with counters to choose.";
                gameLogService.append(gameData, GameLog.text(logEntry));
            } else {
                playerInputService.beginMultiPermanentChoice(gameData, playerId, eligiblePermanentIds,
                        eligiblePermanentIds.size(),
                        new MultiPermanentChoiceContext.Proliferate(remainingProliferates),
                        "Proliferate: Choose permanents to add counters to.");
                return;
            }
        }

        // All proliferates done — now check SBA
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapSubtypeBoost(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.TapSubtypeBoost context) {
        UUID sourcePermanentId = context.sourcePermanentId();

        int count = permanentIds.size();

        if (count == 0) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to tap any Myr.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        } else {
            // Tap the chosen permanents
            List<Card> tappedCards = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    perm.tap();
                    triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                    tappedCards.add(perm.getCard());
                }
            }

            if (!tappedCards.isEmpty()) {
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId) + " taps "
                                + tappedCards.size() + " Myr: "), tappedCards).text(".").build());
                log.info("Game {} - {} taps {} Myr for attack trigger", gameData.id,
                        gameData.playerIdToName.get(playerId), tappedCards.size());
            }

            // Boost source permanent +X/+0 (only if still on battlefield)
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            Card sourceCard = sourcePermanent != null ? sourcePermanent.getCard() : null;
            String sourceName;
            if (sourcePermanent != null) {
                sourcePermanent.setPowerModifier(sourcePermanent.getPowerModifier() + count);
                sourceName = sourcePermanent.getCard().getName();
                gameLogService.append(gameData,
                        GameLog.cardThen(sourceCard, " gets +" + count + "/+0 until end of turn."));
                log.info("Game {} - {} gets +{}/+0", gameData.id, sourceName, count);
            } else {
                sourceName = "Myr Battlesphere";
                log.info("Game {} - Source permanent no longer on battlefield, skipping boost", gameData.id);
            }

            // Deal X damage to the defending player (happens even if source left battlefield per ruling)
            UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, playerId);
            String defenderName = gameData.playerIdToName.get(defendingPlayerId);

            // Check source damage prevention
            Set<UUID> preventedSources = gameData.playerSourceDamagePreventionIds.get(defendingPlayerId);
            boolean sourcePrevented = preventedSources != null && preventedSources.contains(sourcePermanentId);

            if (sourcePrevented) {
                gameLogService.append(gameData, appendCardOrText(GameLog.builder(), sourceCard, sourceName)
                        .text("'s damage to " + defenderName + " is prevented.").build());
            } else {
                // Apply damage multiplier (GlobalDamageMultiplyingEffect)
                int damage = count;
                final int[] multiplier = {1};
                gameData.forEachPermanent((pid, p) -> {
                    for (CardEffect e : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (e instanceof GlobalDamageMultiplyingEffect multiplyingEffect) {
                            multiplier[0] *= multiplyingEffect.damageMultiplierFactor();
                        }
                    }
                });
                damage *= multiplier[0];

                // Apply global prevention shield
                if (gameData.globalDamagePreventionShield > 0 && damage > 0) {
                    int prevented = Math.min(gameData.globalDamagePreventionShield, damage);
                    gameData.globalDamagePreventionShield -= prevented;
                    damage -= prevented;
                }

                damage = damagePreventionService.applyDamagePreventionLifeGainShield(
                        gameData, defendingPlayerId, damage);

                // Apply player prevention shield
                int shield = gameData.playerDamagePreventionShields.getOrDefault(defendingPlayerId, 0);
                if (shield > 0 && damage > 0) {
                    int prevented = Math.min(shield, damage);
                    gameData.playerDamagePreventionShields.put(defendingPlayerId, shield - prevented);
                    damage -= prevented;
                }

                if (damage > 0) {
                    boolean hasInfect = sourcePermanent != null
                            && gameQueryService.hasKeyword(gameData, sourcePermanent, Keyword.INFECT);
                    boolean treatAsInfect = hasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, defendingPlayerId);
                    if (treatAsInfect && gameQueryService.canPlayerGetPoisonCounters(gameData, defendingPlayerId)) {
                        int currentPoison = gameData.playerPoisonCounters.getOrDefault(defendingPlayerId, 0);
                        gameData.playerPoisonCounters.put(defendingPlayerId, currentPoison + damage);
                        GameLog.Builder poisonLog = GameLog.builder().text(defenderName + " gets "
                                + damage + " poison counter" + (damage > 1 ? "s" : "") + " from ");
                        gameLogService.append(gameData,
                                appendCardOrText(poisonLog, sourceCard, sourceName).text(".").build());
                    } else if (!gameQueryService.canPlayerLifeChange(gameData, defendingPlayerId)) {
                        gameLogService.append(gameData, GameLog.text(defenderName + "'s life total can't change."));
                    } else {
                        int currentLife = gameData.getLife(defendingPlayerId);
                        gameData.playerLifeTotals.put(defendingPlayerId, currentLife - damage);
                        gameLogService.append(gameData, appendCardOrText(GameLog.builder(), sourceCard, sourceName)
                                .text(" deals " + damage + " damage to " + defenderName + ".").build());
                    }
                    gameData.recordDamageToPlayer(defendingPlayerId, damage);
                }
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapCreaturesGainLife(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                            MultiPermanentChoiceContext.TapCreaturesGainLife context) {
        List<Card> tappedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !perm.isTapped()) {
                perm.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                tappedCards.add(perm.getCard());
            }
        }

        int tappedCount = tappedCards.size();
        if (tappedCount == 0) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " taps no creatures."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId)
                            + " taps " + tappedCount + " creature" + (tappedCount == 1 ? "" : "s") + ": "), tappedCards)
                            .text(".").build());
            lifeSupport.applyGainLife(gameData, playerId, context.lifePerCreature() * tappedCount);
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleTapCreaturesCreateTokens(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                                MultiPermanentChoiceContext.TapCreaturesCreateTokens context) {
        List<Card> tappedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !perm.isTapped()) {
                perm.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                tappedCards.add(perm.getCard());
            }
        }

        int tappedCount = tappedCards.size();
        if (tappedCount == 0) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " taps no creatures."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(gameData.playerIdToName.get(playerId)
                            + " taps " + tappedCount + " creature" + (tappedCount == 1 ? "" : "s") + ": "), tappedCards)
                            .text(".").build());
            permanentControlSupport.applyCreateToken(gameData, playerId, context.tokenTemplate(), tappedCount,
                    context.sourceSetCode());
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleDevourSacrifice(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.DevourSacrifice context) {
        Permanent entering = gameQueryService.findPermanentById(gameData, context.enteringPermanentId());

        int devoured = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                if (entering != null) {
                    entering.recordDevouredCreature(perm.getCard());
                }
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                devoured++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (entering != null && devoured > 0) {
            if (!gameQueryService.cantHaveCounters(gameData, entering)) {
                int added = context.multiplier() * devoured;
                added = gameQueryService.doublePlusOnePlusOneCounters(gameData, playerId, added);
                entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + added);
                gameLogService.append(gameData, GameLog.cardThen(context.card(),
                        " devours " + devoured + " creature" + (devoured == 1 ? "" : "s")
                                + " and enters with " + added + " +1/+1 counter" + (added == 1 ? "" : "s") + "."));
            }
        }

        // Resume the entry: run ETB triggers now that the devour counters/count are set.
        battlefieldEntryService.processCreatureETBEffects(gameData, context.controllerId(), context.card(),
                context.targetId(), context.wasCastFromHand(), context.etbMode(), context.kicked());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private void handlePayManaPerCreatureUntap(GameData gameData, List<UUID> permanentIds,
                                               MultiPermanentChoiceContext.PayManaPerCreatureUntap context) {
        UUID actingPlayerId = context.actingPlayerId();
        String playerName = gameData.playerIdToName.get(actingPlayerId);

        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " untaps no creatures."));
        } else {
            int totalCost = context.manaPerCreature() * permanentIds.size();
            com.github.laxika.magicalvibes.model.ManaCost cost =
                    new com.github.laxika.magicalvibes.model.ManaCost("{" + totalCost + "}");
            com.github.laxika.magicalvibes.model.ManaPool pool = gameData.playerManaPools.get(actingPlayerId);
            if (pool != null && cost.canPay(pool)) {
                cost.pay(pool);
                List<Card> untappedCards = new ArrayList<>();
                for (UUID permId : permanentIds) {
                    Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                    if (perm != null && tapUntapSupport.untapPermanent(gameData, perm)) {
                        untappedCards.add(perm.getCard());
                    }
                }
                gameLogService.append(gameData,
                        appendCards(GameLog.builder().text(playerName + " pays {" + totalCost + "} and untaps "),
                                untappedCards).text(".").build());
                log.info("Game {} - {} pays {} to untap {} creature(s)", gameData.id, playerName, totalCost,
                        untappedCards.size());
            } else {
                gameLogService.append(gameData, GameLog.text(playerName
                        + " can't pay {" + totalCost + "} — untaps no creatures."));
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleKillingWaveKeep(GameData gameData, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.KillingWaveKeep context) {
        killingWaveEffectHandler.completeKeepChoice(gameData, permanentIds, context);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleExileTetraviteTokensPutCountersOnSource(GameData gameData, List<UUID> permanentIds,
                                                               MultiPermanentChoiceContext.ExileTetraviteTokensPutCountersOnSource context) {
        UUID sourceId = context.sourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        String sourceName = source != null ? source.getCard().getName() : "Tetravus";
        Set<UUID> created = gameData.sourceCreatedTokens.get(sourceId);

        int exiled = 0;
        for (UUID permId : permanentIds) {
            Permanent token = gameQueryService.findPermanentById(gameData, permId);
            if (token != null) {
                permanentRemovalService.removePermanentToExile(gameData, token);
                if (created != null) {
                    created.remove(permId);
                }
                exiled++;
            }
        }

        if (exiled > 0) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            GameLog.Builder tetraviteLog = GameLog.builder().text(
                    exiled + " token" + (exiled == 1 ? "" : "s") + " created with ");
            gameLogService.append(gameData,
                    appendCardOrText(tetraviteLog, source != null ? source.getCard() : null, sourceName)
                            .text((exiled == 1 ? " is" : " are") + " exiled.").build());
            // "Put that many +1/+1 counters on this creature" — only if the source is still around.
            if (source != null) {
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, null, source, exiled);
            }
            log.info("Game {} - {} tokens created with {} exiled to add +1/+1 counters",
                    gameData.id, exiled, sourceName);
        }

        // Standard completion: SBA → may abilities → resume effects
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void handleStaticOrbUntap(GameData gameData, List<UUID> permanentIds,
                                      MultiPermanentChoiceContext.StaticOrbUntap context) {
        UUID activePlayerId = context.activePlayerId();

        List<Card> untappedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                untappedCards.add(perm.getCard());
            }
        }
        String playerName = gameData.playerIdToName.get(activePlayerId);
        if (untappedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " untaps no permanents (untap lock)."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(playerName + " untaps "), untappedCards)
                            .text(" (untap lock).").build());
        }

        // Resume the untap step: of the permanents matching the lock's filter only the chosen ones
        // untap; permanents the filter excludes untap normally, and the rest of the untap-step
        // bookkeeping and turn advance proceed as normal.
        turnProgressionService.resumeStaticOrbUntap(gameData, activePlayerId, new HashSet<>(permanentIds),
                context.filter());
    }

    private void handleCapriciousEfreetOpponentTargets(GameData gameData, List<UUID> permanentIds) {
        PendingCapriciousEfreetState state = gameData.pollPendingInteraction(PendingCapriciousEfreetState.class);

        // Combine own target + opponent targets
        List<UUID> allTargets = new ArrayList<>();
        allTargets.add(state.ownTargetId());
        allTargets.addAll(permanentIds);

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                state.sourceCard(),
                state.controllerId(),
                state.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DestroyOneOfTargetsAtRandomEffect())),
                0,
                allTargets
        );
        gameData.stack.add(entry);

        List<String> targetNames = new ArrayList<>();
        GameLog.Builder targetsLog = GameLog.builder().card(state.sourceCard()).text("'s ability targets ");
        for (int i = 0; i < allTargets.size(); i++) {
            UUID targetId = allTargets.get(i);
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            targetNames.add(target != null ? target.getCard().getName() : targetId.toString());
            if (i > 0) {
                targetsLog.text(", ");
            }
            appendCardOrText(targetsLog, target != null ? target.getCard() : null, targetId.toString());
        }
        targetsLog.text(".");
        gameLogService.append(gameData, targetsLog.build());
        log.info("Game {} - {} upkeep trigger targets: {}", gameData.id, state.sourceCard().getName(), targetNames);

        // Continue processing: more Efreet triggers → may abilities → priority
        if (gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
            turnProgressionService.processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            turnProgressionService.processNextPucasMischiefTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handlePileSeparation(GameData gameData, List<UUID> permanentIds) {
        destructionSupport.completePileSeparationStep1(gameData, permanentIds);
    }

    /** Appends {@code cards} as comma-separated card segments (each hoverable) to {@code builder}. */
    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder;
    }

    /** Appends a hoverable card segment when {@code card} is known, otherwise falls back to plain text. */
    private static GameLog.Builder appendCardOrText(GameLog.Builder builder, Card card, String fallbackText) {
        if (card != null) {
            builder.card(card);
        } else {
            builder.text(fallbackText);
        }
        return builder;
    }
}
