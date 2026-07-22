package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.effect.MayEffectHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MayAbilityHandlerService {

    private final InputCompletionService inputCompletionService;
    private final MayCastHandlerService mayCastHandlerService;
    private final MayCopyHandlerService mayCopyHandlerService;
    private final MayMiscHandlerService mayMiscHandlerService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final DestructionSupport destructionSupport;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport brilliantUltimatumSupport;
    private final MayAbilityTapCostService mayAbilityTapCostService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final ValidTargetService validTargetService;
    private final MayEffectHandlerRegistry mayEffectHandlerRegistry;

    public MayAbilityHandlerService(InputCompletionService inputCompletionService,
                                    MayCastHandlerService mayCastHandlerService,
                                    MayCopyHandlerService mayCopyHandlerService,
                                    MayMiscHandlerService mayMiscHandlerService,
                                    GameQueryService gameQueryService,
                                    PredicateEvaluationService predicateEvaluationService,
                                    GameBroadcastService gameBroadcastService,
                                    PlayerInputService playerInputService,
                                    TurnProgressionService turnProgressionService,
                                    EffectResolutionService effectResolutionService,
                                    DestructionSupport destructionSupport,
                                    GraveyardReturnSupport graveyardReturnSupport,
                                    com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport brilliantUltimatumSupport,
                                    MayAbilityTapCostService mayAbilityTapCostService,
                                    com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry,
                                    ValidTargetService validTargetService,
                                    MayEffectHandlerRegistry mayEffectHandlerRegistry) {
        this.inputCompletionService = inputCompletionService;
        this.mayCastHandlerService = mayCastHandlerService;
        this.mayCopyHandlerService = mayCopyHandlerService;
        this.mayMiscHandlerService = mayMiscHandlerService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameBroadcastService = gameBroadcastService;
        this.playerInputService = playerInputService;
        this.turnProgressionService = turnProgressionService;
        this.effectResolutionService = effectResolutionService;
        this.destructionSupport = destructionSupport;
        this.graveyardReturnSupport = graveyardReturnSupport;
        this.brilliantUltimatumSupport = brilliantUltimatumSupport;
        this.mayAbilityTapCostService = mayAbilityTapCostService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.validTargetService = validTargetService;
        this.mayEffectHandlerRegistry = mayEffectHandlerRegistry;
    }

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) == null) {
            throw new IllegalStateException("Not awaiting may ability choice");
        }
        PendingInteraction.MayAbilityChoice mayAbilityChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        if (mayAbilityChoice == null || !player.getId().equals(mayAbilityChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PendingMayAbility ability = gameData.pendingMayAbilities.removeFirst();
        gameData.interaction.clearAwaitingInput();

        // Pile separation: permanent-pile (Liliana) vs card-pile (Boneyard Parley, Brilliant Ultimatum, Unesh)
        PendingPileSeparation pileSeparation = gameData.peekPendingInteraction(PendingPileSeparation.class);
        if (pileSeparation != null) {
            if (pileSeparation.disposition() == CardPileDisposition.PLAY_FROM_EXILE) {
                brilliantUltimatumSupport.completePileSeparationStep2(gameData, accepted);
            } else if (pileSeparation.cardPileMode()) {
                // BATTLEFIELD (Boneyard Parley) and HAND (Unesh) both flow through step 2, which branches on disposition.
                graveyardReturnSupport.completeCardPileSeparationStep2(gameData, accepted);
            } else {
                destructionSupport.completePileSeparationStep2(gameData, accepted);
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // CR 603.5: resolution-time "you may" choice for triggered abilities on the stack.
        if (gameData.resolvingMayEffectFromStack) {
            handleResolutionTimeMayChoice(gameData, player, accepted, ability);
            return;
        }

        // Pending equipment attach — e.g. Auriok Survivors "you may attach it to this creature"
        UUID pendingEquipId = gameData.interaction.pendingEquipmentAttachEquipmentId();
        UUID pendingTargetId = gameData.interaction.pendingEquipmentAttachTargetId();
        if (pendingEquipId != null && pendingTargetId != null) {
            mayMiscHandlerService.handleEquipmentAttachChoice(gameData, player, accepted, pendingEquipId, pendingTargetId);
            return;
        }

        // Registry dispatch (mayfx): route to the first effect in the ability's list that has a
        // migrated handler. No card bundles two registered may-effect types at the top level of
        // effects(), so list order matches the old fixed code-order chain (see refactor-docs).
        for (CardEffect effect : ability.effects()) {
            var mayHandler = mayEffectHandlerRegistry.getHandler(effect);
            if (mayHandler != null) {
                mayHandler.handle(gameData, player, accepted, ability);
                return;
            }
        }

        // Cast-from-library-without-paying — e.g. Galvanoth (second phase: cast prompt)
        CastTopOfLibraryWithoutPayingManaCostEffect castFromLibEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTopOfLibraryWithoutPayingManaCostEffect)
                .map(e -> (CastTopOfLibraryWithoutPayingManaCostEffect) e)
                .findFirst().orElse(null);
        if (castFromLibEffect != null && castFromLibEffect.castableTypes().contains(ability.sourceCard().getType())) {
            mayCastHandlerService.handleCastFromLibraryChoice(gameData, player, accepted, ability);
            return;
        }

        // Mana payment for may-pay triggers (e.g. Embersmith "pay {1}", Vigil for the Lost "pay {X}")
        int xValuePaid = 0;
        if (accepted && ability.manaCost() != null) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());

            if (cost.hasX()) {
                // X cost: pay all available mana as X
                int maxX = cost.calculateMaxX(pool);
                if (maxX <= 0) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            player.getUsername() + " has no mana to pay for ", ability.sourceCard(), "'s ability."));
                    log.info("Game {} - {} has no mana for X may ability", gameData.id, player.getUsername());

                    inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                xValuePaid = maxX;
                cost.pay(pool, maxX);
            } else {
                if (!cost.canPay(pool)) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            player.getUsername() + " cannot pay " + ability.manaCost() + " for ", ability.sourceCard(), "'s ability."));
                    log.info("Game {} - {} can't pay {} for may ability", gameData.id, player.getUsername(), ability.manaCost());

                    inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                cost.pay(pool);
            }
        }

        // Targeted may ability (e.g. "you may deal 3 damage to target creature", "you may destroy target Equipment")
        boolean isTargetedPermanentEffect = ability.effects().stream()
                .anyMatch(e -> e.targetSpec().category().includesPermanents());
        boolean isTargetedPlayerEffect = ability.effects().stream()
                .anyMatch(e -> e.targetSpec().category().includesPlayers());
        boolean isTargetedGraveyardEffect = ability.effects().stream()
                .anyMatch(e -> e.targetSpec().category().isGraveyard());
        boolean isTargetedEffect = isTargetedPermanentEffect || isTargetedPlayerEffect || isTargetedGraveyardEffect;

        // Pre-targeted may ability — target was already chosen (e.g. "You may tap or untap that creature", "you may have that player lose 1 life")
        if (accepted && isTargetedEffect && ability.targetCardId() != null) {
            boolean isPreTargetedPlayer = gameData.playerIds.contains(ability.targetCardId());
            Permanent target = isPreTargetedPlayer ? null : gameQueryService.findPermanentById(gameData, ability.targetCardId());
            if (target != null || isPreTargetedPlayer) {
                StackEntry entry;
                if (ability.sourcePermanentId() != null) {
                    entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            ability.sourceCard(),
                            ability.controllerId(),
                            ability.sourceCard().getName() + "'s ability",
                            new ArrayList<>(ability.effects()),
                            null,
                            ability.sourcePermanentId()
                    );
                } else {
                    entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            ability.sourceCard(),
                            ability.controllerId(),
                            ability.sourceCard().getName() + "'s ability",
                            new ArrayList<>(ability.effects()),
                            0
                    );
                }
                entry.setTargetId(ability.targetCardId());
                gameData.stack.add(entry);

                if (isPreTargetedPlayer) {
                    String targetName = gameData.playerIdToName.get(ability.targetCardId());
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            player.getUsername() + " accepts — ", ability.sourceCard(), "'s ability targets " + targetName + "."));
                } else {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                            .text(player.getUsername() + " accepts — ")
                            .card(ability.sourceCard())
                            .text("'s ability targets ")
                            .card(target.getCard())
                            .text(".")
                            .build());
                }
                log.info("Game {} - {} accepts pre-targeted may ability from {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            } else {
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(ability.sourceCard(), "'s ability fizzles — target no longer exists."));
                log.info("Game {} - {} pre-targeted may ability target gone", gameData.id, ability.sourceCard().getName());
            }

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && isTargetedGraveyardEffect) {
            handleGraveyardTargetedMayAbility(gameData, player, ability);
            return;
        }

        if (accepted && isTargetedEffect) {
            handleTargetedMayAbilityAccepted(gameData, player, ability);
            return;
        }

        if (accepted) {
            StackEntry entry;
            if (ability.sourcePermanentId() != null) {
                // Combat damage trigger with source permanent and target context
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects()),
                        ability.targetCardId(),
                        ability.sourcePermanentId()
                );
            } else {
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects()),
                        xValuePaid
                );
            }

            // Self-targeting effects need the source permanent's ID to resolve
            boolean needsSelfTarget = ability.effects().stream().anyMatch(e ->
                    e instanceof PutCountersOnSelfEffect
                            || (e instanceof AnimatePermanentsEffect animate && animate.scope() == GrantScope.SELF)
                            || e instanceof BoostSelfEffect
                            || e instanceof ImprintDyingCreatureEffect
                            || e instanceof ExileFromHandToImprintEffect
                            || e instanceof ReturnDyingCreatureToBattlefieldAndAttachSourceEffect
                            || e instanceof BecomeCopyOfDyingCreatureEffect);
            if (needsSelfTarget) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(ability.controllerId());
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getCard() == ability.sourceCard()) {
                            entry.setTargetId(p.getId());
                            break;
                        }
                    }
                }
            }

            // Effects that copy an entering permanent need the target permanent ID from the trigger
            boolean needsEnteringTarget = ability.effects().stream()
                    .anyMatch(e -> e instanceof CreateTokenCopyOfTargetPermanentEffect);
            if (needsEnteringTarget && ability.targetCardId() != null) {
                entry.setTargetId(ability.targetCardId());
            }

            gameData.stack.add(entry);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    player.getUsername() + " accepts — ", ability.sourceCard(), "'s triggered ability goes on the stack."));
            log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .text(player.getUsername() + " declines ")
                    .card(ability.sourceCard())
                    .text("'s triggered ability.")
                    .build());
            log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleTargetedMayAbilityAccepted(GameData gameData, Player player, PendingMayAbility ability) {
        // Collect valid permanent targets from all battlefields using the may-ability's target filter
        List<UUID> validTargets = new ArrayList<>();
        Card sourceCard = ability.sourceCard();
        TargetFilter targetFilter = mayAbilityTargetFilter(sourceCard, ability);
        boolean canTargetPermanent = ability.effects().stream().anyMatch(e -> e.targetSpec().category().includesPermanents());
        if (canTargetPermanent) {
            FilterContext ctx = FilterContext.of(gameData)
                    .withSourceCardId(sourceCard.getId())
                    .withSourceControllerId(ability.controllerId());
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (targetFilter instanceof PermanentPredicateTargetFilter filter) {
                        if (predicateEvaluationService.matchesPermanentPredicate(p, filter.predicate(), ctx)) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        // Add player IDs for effects that can target players (e.g. DealDamageToAnyTargetEffect, MillEffect),
        // honoring the card's player target filter (e.g. "target opponent") so the controller is excluded.
        boolean canTargetPlayer = ability.effects().stream().anyMatch(e -> e.targetSpec().category().includesPlayers());
        if (canTargetPlayer) {
            validTargets.addAll(validTargetService.filterValidPlayerTargets(
                    gameData, targetFilter, gameData.orderedPlayerIds, ability.controllerId()));
        }

        if (validTargets.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(ability.sourceCard(), "'s ability has no valid targets."));
            log.info("Game {} - {} may ability has no valid targets", gameData.id, ability.sourceCard().getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                ability.sourceCard(), ability.controllerId(), new ArrayList<>(ability.effects())
        ));
        String targetDescription;
        if (!canTargetPermanent && canTargetPlayer) {
            targetDescription = "player";
        } else if (targetFilter instanceof PermanentPredicateTargetFilter filter) {
            targetDescription = filter.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (canTargetPlayer) {
            targetDescription = "any target";
        } else {
            targetDescription = "creature";
        }
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                ability.sourceCard().getName() + "'s ability — Choose target " + targetDescription + ".");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                player.getUsername() + " accepts — choosing a target for ", ability.sourceCard(), "'s ability."));
        log.info("Game {} - {} accepts targeted may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
    }

    private void handleGraveyardTargetedMayAbility(GameData gameData, Player player, PendingMayAbility ability) {
        UUID controllerId = ability.controllerId();

        // Determine filter from the graveyard-targeting effect
        CardPredicate filter = null;
        boolean anyGraveyard = false;
        for (CardEffect effect : ability.effects()) {
            if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect imprint) {
                filter = imprint.filter();
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
            if (effect instanceof ExileTargetCardFromGraveyardAndCreateTokenCopyEffect exileCopy) {
                filter = exileCopy.filter();
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
            if (effect instanceof ReturnCardFromGraveyardEffect ret) {
                filter = ret.filter();
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
            if (effect.targetSpec().category().isGraveyard()) {
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
        }

        // Collect matching graveyard cards
        List<UUID> searchPlayerIds = anyGraveyard
                ? gameData.orderedPlayerIds
                : List.of(controllerId);
        UUID graveyardOwnerId = null;
        List<Integer> matchingIndices = new ArrayList<>();
        for (UUID pid : searchPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard == null) continue;
            for (int i = 0; i < graveyard.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), filter, ability.sourceCard().getId())) {
                    matchingIndices.add(i);
                    graveyardOwnerId = pid;
                }
            }
        }

        if (matchingIndices.isEmpty()) {
            String filterLabel = CardPredicateUtils.describeFilter(filter);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(
                    ability.sourceCard(), "'s ability has no valid " + filterLabel + " targets in graveyard."));
            log.info("Game {} - {} may ability has no valid graveyard targets", gameData.id, ability.sourceCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // If only one match, create stack entry immediately
        if (matchingIndices.size() == 1) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            Card targetCard = graveyard.get(matchingIndices.getFirst());

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    controllerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects()),
                    targetCard.getId(),
                    ability.sourcePermanentId()
            );
            gameData.stack.add(entry);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .text(player.getUsername() + " accepts — ")
                    .card(ability.sourceCard())
                    .text("'s ability targets ")
                    .card(targetCard)
                    .text(" in graveyard.")
                    .build());
            log.info("Game {} - {} accepts graveyard-targeted may ability from {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Multiple matches — prompt player to choose
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(graveyardOwnerId, matchingIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose a " + filterLabel + " from your graveyard to target.")
                .mayAbilityContext(ability.sourceCard(), controllerId,
                        new ArrayList<>(ability.effects()), ability.sourcePermanentId())
                .build());

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                player.getUsername() + " accepts — choosing a graveyard target for ", ability.sourceCard(), "'s ability."));
        log.info("Game {} - {} accepts graveyard-targeted may ability from {}", gameData.id,
                player.getUsername(), ability.sourceCard().getName());
    }

    private void handleResolutionTimeMayChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        gameData.resolvingMayEffectFromStack = false;
        RegisterDelayedCounterTriggerEffect dct = ability.effects().stream().filter(e -> e instanceof RegisterDelayedCounterTriggerEffect).map(e -> (RegisterDelayedCounterTriggerEffect) e).findFirst().orElse(null);
        if (dct != null) { gameData.pendingEffectResolutionEntry = null; gameData.pendingEffectResolutionIndex = 0; mayMiscHandlerService.handleOpeningHandDelayedCounterTrigger(gameData, player, accepted, ability, dct); return; }
        RegisterDelayedManaTriggerEffect dmt = ability.effects().stream().filter(e -> e instanceof RegisterDelayedManaTriggerEffect).map(e -> (RegisterDelayedManaTriggerEffect) e).findFirst().orElse(null);
        if (dmt != null) { gameData.pendingEffectResolutionEntry = null; gameData.pendingEffectResolutionIndex = 0; mayMiscHandlerService.handleOpeningHandDelayedManaTrigger(gameData, player, accepted, ability, dmt); return; }
        // Redirect retarget — ChooseNewTargetsForTargetSpellEffect needs the full retarget UI flow
        boolean isRedirectRetarget = ability.effects().stream().anyMatch(e -> e instanceof ChooseNewTargetsForTargetSpellEffect);
        if (isRedirectRetarget) {
            // Advance past this "may choose new targets" effect (the stored index points at the
            // MayEffect for re-run) so any following effects on the same spell continue after the
            // retarget completes — e.g. Wild Ricochet's "Then copy that spell." The entry is kept
            // so the retarget-completion path (handleSpellRetarget / the decline branch) resumes
            // the remaining effects. For a standalone Redirect the resumed index is past the end,
            // so it is a harmless no-op.
            if (gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionIndex = gameData.pendingEffectResolutionIndex + 1;
            }
            mayCopyHandlerService.handleRedirectRetargetChoice(gameData, player, accepted, ability);
            return;
        }
        if (accepted) {
            if (ability.tapPermanentsCost() != null) {
                // beginTapCostPayment either awaits player input or (on auto-pay / failure
                // to pay) already resumes stack resolution itself — nothing left to do here.
                mayAbilityTapCostService.beginTapCostPayment(
                        gameData, player, ability.tapPermanentsCost(), ability.sourcePermanentId());
                return;
            } else if (ability.manaCost() != null) {
                ManaCost cost = new ManaCost(ability.manaCost());
                ManaPool pool = gameData.playerManaPools.get(player.getId());
                if (!cost.canPay(pool)) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            player.getUsername() + " cannot pay " + ability.manaCost() + " for ", ability.sourceCard(), "'s ability."));
                    gameData.resolvedMayAccepted = false;
                    if (gameData.pendingEffectResolutionEntry != null) { effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex); }
                    if (gameData.interaction.isAwaitingInput()) { return; }
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                cost.pay(pool);
            }
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    player.getUsername() + " accepts — resolving ", ability.sourceCard(), "'s ability."));
            CardEffect innerEffect = extractInnerEffect(ability);
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            boolean isTargetedPermanent = innerEffect != null && innerEffect.targetSpec().category().includesPermanents();
            boolean isTargetedPlayer = innerEffect != null && innerEffect.targetSpec().category().includesPlayers();
            boolean isTargetedGraveyard = innerEffect != null && innerEffect.targetSpec().category().isGraveyard();
            boolean targetAlreadySet = pendingEntry != null
                    && (pendingEntry.getTargetId() != null || !pendingEntry.getTargetIds().isEmpty());
            if ((isTargetedPermanent || isTargetedPlayer) && pendingEntry != null && !targetAlreadySet) {
                gameData.resolvedMayAccepted = true;
                handleResolutionTimeTargetSelection(gameData, player, ability, pendingEntry, isTargetedPermanent, isTargetedPlayer);
                return;
            }
            if (isTargetedGraveyard && pendingEntry != null && !targetAlreadySet) {
                gameData.resolvedMayAccepted = true;
                handleResolutionTimeGraveyardTargetSelection(gameData, player, ability, pendingEntry);
                return;
            }
            if (pendingEntry != null) { setUpSelfTargetIfNeeded(gameData, ability, pendingEntry, innerEffect); }
            gameData.resolvedMayAccepted = true;
        } else {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.playerDeclinesAbility(player.getUsername(), ability.sourceCard()));
            gameData.resolvedMayAccepted = false;
        }
        if (gameData.pendingEffectResolutionEntry != null) { effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex); }
        if (gameData.interaction.isAwaitingInput()) { return; }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * The target filter that governs a targeted may-ability's target choice. Prefers the filter of
     * the card target group the may-ability's own effect is bound to, so a multi-target card whose
     * may-ability targets a different set than its primary target picks the right filter — e.g. an
     * aura that enchants a "creature you control" but whose ETB "you may put a -1/-1 counter on
     * target creature" may hit ANY creature (Cartouche of Ambition). Falls back to the card's
     * primary target filter when the effect is not bound to a declared target group (single-target
     * may abilities are bound to group 0, whose filter equals {@code getTargetFilter()}).
     */
    private TargetFilter mayAbilityTargetFilter(Card sourceCard, PendingMayAbility ability) {
        for (CardEffect e : ability.effects()) {
            int idx = sourceCard.getEffectTargetIndex(e);
            if (idx >= 0 && idx < sourceCard.getSpellTargets().size()) {
                TargetFilter groupFilter = sourceCard.getSpellTargets().get(idx).getFilter();
                if (groupFilter != null) {
                    return groupFilter;
                }
            }
        }
        return sourceCard.getTargetFilter();
    }

    private CardEffect extractInnerEffect(PendingMayAbility ability) {
        if (ability.effects().isEmpty()) return null;
        CardEffect first = ability.effects().getFirst();
        if (first instanceof MayEffect may) { return may.wrapped(); }
        return first;
    }

    private void setUpSelfTargetIfNeeded(GameData gameData, PendingMayAbility ability, StackEntry pendingEntry, CardEffect innerEffect) {
        if (innerEffect == null) return;
        boolean needsSelfTarget = innerEffect instanceof PutCountersOnSelfEffect || (innerEffect instanceof AnimatePermanentsEffect animate && animate.scope() == GrantScope.SELF) || innerEffect instanceof BoostSelfEffect || innerEffect instanceof ImprintDyingCreatureEffect || innerEffect instanceof ExileFromHandToImprintEffect || innerEffect instanceof ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
        if (needsSelfTarget && pendingEntry.getTargetId() == null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(ability.controllerId());
            if (battlefield != null) { for (Permanent p : battlefield) { if (p.getCard() == ability.sourceCard()) { pendingEntry.setTargetId(p.getId()); break; } } }
        }
    }

    private void handleResolutionTimeGraveyardTargetSelection(GameData gameData, Player player,
                                                              PendingMayAbility ability, StackEntry pendingEntry) {
        UUID controllerId = ability.controllerId();

        // Determine filter from the graveyard-targeting effect
        CardPredicate filter = null;
        boolean anyGraveyard = false;
        for (CardEffect effect : ability.effects()) {
            if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect imprint) {
                filter = imprint.filter();
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
            if (effect instanceof ExileTargetCardFromGraveyardAndCreateTokenCopyEffect exileCopy) {
                filter = exileCopy.filter();
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
            if (effect instanceof ReturnCardFromGraveyardEffect ret) {
                filter = ret.filter();
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
            if (effect.targetSpec().category().isGraveyard()) {
                anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                break;
            }
        }

        List<UUID> searchPlayerIds = anyGraveyard
                ? gameData.orderedPlayerIds
                : List.of(controllerId);
        UUID graveyardOwnerId = null;
        List<Integer> matchingIndices = new ArrayList<>();
        for (UUID pid : searchPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard == null) continue;
            for (int i = 0; i < graveyard.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), filter, ability.sourceCard().getId())) {
                    matchingIndices.add(i);
                    graveyardOwnerId = pid;
                }
            }
        }

        if (matchingIndices.isEmpty()) {
            String filterLabel = CardPredicateUtils.describeFilter(filter);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(
                    ability.sourceCard(), "'s ability — no valid " + filterLabel + " targets in graveyard."));
            // Resume resolution with may declined (no valid target)
            gameData.resolvedMayAccepted = false;
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        // Single match — set target immediately and resume resolution
        if (matchingIndices.size() == 1) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            Card targetCard = graveyard.get(matchingIndices.getFirst());
            pendingEntry.setTargetId(targetCard.getId());

            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    player.getUsername() + " targets ", targetCard, " in graveyard."));
            effectResolutionService.resolveEffectsFrom(gameData, pendingEntry, gameData.pendingEffectResolutionIndex);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        // Multiple matches — prompt player to choose via graveyard choice
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        gameData.resolvedMayTargetingEntry = pendingEntry;
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(graveyardOwnerId, matchingIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose a " + filterLabel + " from your graveyard to target.")
                .mayAbilityContext(ability.sourceCard(), controllerId,
                        new ArrayList<>(ability.effects()), ability.sourcePermanentId())
                .build());
    }

    private void handleResolutionTimeTargetSelection(GameData gameData, Player player, PendingMayAbility ability, StackEntry pendingEntry, boolean canTargetPermanent, boolean canTargetPlayer) {
        List<UUID> validTargets = new ArrayList<>();
        Card sourceCard = ability.sourceCard();
        TargetFilter targetFilter = mayAbilityTargetFilter(sourceCard, ability);
        if (canTargetPermanent) { FilterContext ctx = FilterContext.of(gameData).withSourceCardId(sourceCard.getId()).withSourceControllerId(ability.controllerId()); for (UUID pid : gameData.orderedPlayerIds) { List<Permanent> battlefield = gameData.playerBattlefields.get(pid); if (battlefield == null) continue; for (Permanent p : battlefield) { if (targetFilter instanceof PermanentPredicateTargetFilter filter) { if (predicateEvaluationService.matchesPermanentPredicate(p, filter.predicate(), ctx)) { validTargets.add(p.getId()); } } else if (gameQueryService.isCreature(gameData, p)) { validTargets.add(p.getId()); } } } }
        if (canTargetPlayer) { validTargets.addAll(validTargetService.filterValidPlayerTargets(gameData, targetFilter, gameData.orderedPlayerIds, ability.controllerId())); }
        if (validTargets.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(ability.sourceCard(), "'s ability has no valid targets."));
            gameData.resolvedMayAccepted = false;
            if (gameData.pendingEffectResolutionEntry != null) { effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex); }
            if (gameData.interaction.isAwaitingInput()) return;
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        gameData.resolvedMayTargetingEntry = pendingEntry;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(ability.sourceCard(), ability.controllerId(), new ArrayList<>(ability.effects())));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets, ability.sourceCard().getName() + "'s ability — Choose target.");
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                player.getUsername() + " accepts — choosing a target for ", ability.sourceCard(), "'s ability."));
    }
}
