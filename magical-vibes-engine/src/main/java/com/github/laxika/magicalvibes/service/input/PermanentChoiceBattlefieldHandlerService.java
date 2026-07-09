package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles permanent choice contexts related to battlefield manipulation.
 *
 * <p>Covers clone copies, aura grafting, legend rule, sacrificing creatures,
 * activated ability cost choices, bouncing, damage prevention source choices,
 * sacrifice-for-divided-damage, and aura ETB placement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceBattlefieldHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CloneService cloneService;
    private final WarpWorldService warpWorldService;
    private final GameBroadcastService gameBroadcastService;
    private final AbilityActivationService abilityActivationService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final StateBasedActionService stateBasedActionService;
    private final TriggerCollectionService triggerCollectionService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final CreatureControlService creatureControlService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final LifeSupport lifeSupport;
    private final LibrarySearchSupport librarySearchSupport;
    private final MayAbilityTapCostService mayAbilityTapCostService;

    public void handleCloneCopy(GameData gameData, UUID permanentId) {
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, permanentId);
        if (targetPerm == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        cloneService.completeCloneEntry(gameData, permanentId);

        if (!gameData.interaction.isAwaitingInput()) {
            stateBasedActionService.performStateBasedActions(gameData);

            if (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
                triggerCollectionService.processNextDeathTriggerTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void handleAuraGraft(GameData gameData, UUID permanentId, PermanentChoiceContext.AuraGraft auraGraft) {
        Permanent aura = gameQueryService.findPermanentById(gameData, auraGraft.auraPermanentId());
        if (aura == null) {
            throw new IllegalStateException("Aura permanent no longer exists");
        }

        Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (newTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        aura.setAttachedTo(permanentId);

        String logEntry = aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleLegendRule(GameData gameData, UUID playerId, UUID permanentId, PermanentChoiceContext.LegendRule legendRule) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> toRemove = new ArrayList<>();
        for (Permanent perm : battlefield) {
            if (perm.getCard().getName().equals(legendRule.cardName()) && !perm.getId().equals(permanentId)) {
                toRemove.add(perm);
            }
        }
        for (Permanent perm : toRemove) {
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificeCreature(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = sacrificeCreature.sacrificingPlayerId();
        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificeCreatureThenSearchLibrary(GameData gameData, UUID permanentId,
                                                         PermanentChoiceContext.SacrificeCreatureThenSearchLibrary context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = context.sacrificingPlayerId();
        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // "If you do" — sacrifice happened, now search library for a creature card
        librarySearchSupport.searchLibraryForCreatureToHand(gameData, sacrificingPlayerId);

        if (!gameData.interaction.isAwaitingInput()) {
            stateBasedActionService.performStateBasedActions(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void handleSacrificeCreatureOpponentsLoseLife(GameData gameData, UUID permanentId,
                                                         PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = context.sacrificingPlayerId();

        // Capture effective power before removing from battlefield (static bonuses still apply)
        int power = gameQueryService.getEffectivePower(gameData, target);

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // Each opponent loses life equal to the sacrificed creature's power
        destructionSupport.applyOpponentsLoseLife(gameData, sacrificingPlayerId, power, context.sourceCardName());

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleForcedCostOrElse(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.ForcedCostOrElse context) {
        destructionSupport.completeForcedCostOrElse(gameData, permanentId, context);

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificeCreatureControllerGainsLifeEqualToToughness(GameData gameData, UUID permanentId,
                                                                            PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = context.sacrificingPlayerId();

        // Capture effective toughness before removing from battlefield (static bonuses still apply)
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // Controller gains life equal to the sacrificed creature's toughness
        lifeSupport.applyGainLife(gameData, context.controllerId(), toughness, context.sourceCardName());

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleActivatedAbilityCostChoice(GameData gameData, Player player, UUID permanentId, PermanentChoiceContext.ActivatedAbilityCostChoice costChoice) {
        abilityActivationService.completeActivatedAbilityCostChoice(gameData, player, costChoice, permanentId);
    }

    public void handleGraveyardAbilityCostChoice(GameData gameData, Player player, UUID permanentId, PermanentChoiceContext.GraveyardAbilityCostChoice graveyardCostChoice) {
        abilityActivationService.completeGraveyardAbilityCostChoice(gameData, player, graveyardCostChoice, permanentId);
    }

    public void handleMayAbilityTapCostChoice(GameData gameData, Player player, UUID permanentId,
                                              PermanentChoiceContext.MayAbilityTapCostChoice mayTapCostChoice) {
        mayAbilityTapCostService.completeTapCostChoice(gameData, player, mayTapCostChoice, permanentId);
    }

    public void handleBounceCreature(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by bounce effect", gameData.id, target.getCard().getName());
        }

        stateBasedActionService.performStateBasedActions(gameData);

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleBounceOwnPermanentOrSacrificeSelf(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by bounce-or-sacrifice effect", gameData.id, target.getCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleChampionCreature(GameData gameData, UUID championedPermanentId,
                                       PermanentChoiceContext.ChampionCreature context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, championedPermanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        Card card = target.getOriginalCard();
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), context.controllerId());

        permanentRemovalService.removePermanentToExile(gameData, target);

        String logEntry = card.getName() + " is exiled by " + source.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} champions {} (exiled until source leaves)",
                gameData.id, source.getCard().getName(), card.getName());

        gameData.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(card, ownerId));

        permanentRemovalService.removeOrphanedAuras(gameData);

        // "When a creature is championed with this permanent, ..." (e.g. Mistbind Clique).
        List<CardEffect> championedEffects = source.getCard().getEffects(EffectSlot.ON_CHAMPIONED);
        if (championedEffects != null && !championedEffects.isEmpty()) {
            beginChampionedTrigger(gameData, source, context.controllerId(), new ArrayList<>(championedEffects));
            return;
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void beginChampionedTrigger(GameData gameData, Permanent source, UUID controllerId,
                                        List<CardEffect> effects) {
        stateBasedActionService.performStateBasedActions(gameData);

        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData, effects, source.getCard().getTargetFilter(), controllerId,
                source.getCard(), TriggerTargetCollector.Options.END_STEP);

        if (result.validTargets().isEmpty()) {
            String logEntry = source.getCard().getName() + "'s championed trigger has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} championed trigger skipped (no valid targets)",
                    gameData.id, source.getCard().getName());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChampionedTriggerTarget(
                        source.getCard(), controllerId, effects, source.getId()));
        playerInputService.beginPermanentChoice(gameData, controllerId, result.validTargets(),
                source.getCard().getName() + "'s ability — Choose target player.");

        String logEntry = source.getCard().getName() + "'s championed trigger — choose target player.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} championed trigger awaiting target selection", gameData.id, source.getCard().getName());
    }

    public void handlePreventDamageSourceChoice(GameData gameData, UUID permanentId, PermanentChoiceContext.PreventDamageSourceChoice preventSource) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID controllerId = preventSource.controllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = chosenPermanent.getCard().getName();

        if (preventSource.controllerOnly()) {
            gameData.playerSourceDamagePreventionIds
                    .computeIfAbsent(controllerId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(permanentId);
            String logEntry = "All damage " + sourceName + " would deal to " + playerName + " is prevented this turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            gameData.permanentsPreventedFromDealingDamage.add(permanentId);
            String logEntry = "All damage " + sourceName + " would deal this turn is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} chose {} as prevented damage source", gameData.id, playerName, sourceName);

        stateBasedActionService.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleRedirectDamageSourceChoice(GameData gameData, UUID permanentId,
                                                  PermanentChoiceContext.RedirectDamageSourceChoice redirectSource) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID controllerId = redirectSource.controllerId();
        gameData.sourceDamageRedirectShields.add(new SourceDamageRedirectShield(
                controllerId, permanentId, redirectSource.amount(), redirectSource.redirectTargetId()));

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "The next " + redirectSource.amount() + " damage " + chosenPermanent.getCard().getName()
                + " would deal to " + playerName + " or permanents " + playerName + " controls is dealt to another target instead.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} chose {} as redirect damage source (up to {} damage redirected)",
                gameData.id, playerName, chosenPermanent.getCard().getName(), redirectSource.amount());

        stateBasedActionService.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handlePreventDamageToTargetFromSourceChoice(GameData gameData, UUID permanentId,
                                                             PermanentChoiceContext.PreventDamageToTargetFromSourceChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID targetId = ctx.targetId();
        gameData.targetSourceDamagePreventionShields.add(new TargetSourceDamagePreventionShield(
                targetId, permanentId, ctx.amount()));

        // Determine target name for logging
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
        String targetName = targetPerm != null
                ? targetPerm.getCard().getName()
                : gameData.playerIdToName.getOrDefault(targetId, "unknown");

        String logEntry = "The next " + ctx.amount() + " damage " + chosenPermanent.getCard().getName()
                + " would deal to " + targetName + " is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Chose {} as damage source, preventing up to {} damage to {}",
                gameData.id, chosenPermanent.getCard().getName(), ctx.amount(), targetName);

        stateBasedActionService.performStateBasedActions(gameData);

        // Resume pending effect resolution (e.g. GainLifeEffect after prevention source choice)
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificeArtifactForDividedDamage(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd) {
        Permanent artifactToSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (artifactToSacrifice == null) {
            throw new IllegalStateException("Artifact permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, artifactToSacrifice);

        String playerName = gameData.playerIdToName.get(sadd.controllerId());
        String logEntry = playerName + " sacrifices " + artifactToSacrifice.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {} for divided damage", gameData.id, playerName, artifactToSacrifice.getCard().getName());

        damageSupport.dealDividedDamageToAnyTargets(
                gameData, sadd.sourceCard(), sadd.controllerId(), sadd.damageAssignments());

        gameData.pendingETBDamageAssignments = Map.of();

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificePermanentThen(GameData gameData, UUID permanentId,
                                              PermanentChoiceContext.SacrificePermanentThen ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        String logEntry = playerName + " sacrifices " + toSacrifice.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        // Execute the "if you do" effect by pushing it onto the stack as a triggered ability.
        // A null thenEffect means a bare "sacrifice a permanent" with no follow-up.
        if (ctx.thenEffect() != null) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ctx.sourceCard(),
                    ctx.controllerId(),
                    ctx.sourceCard().getName() + "'s effect",
                    new ArrayList<>(List.of(ctx.thenEffect()))
            ));
        }

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificeCreatureCreateTokensEqualToToughness(GameData gameData, UUID permanentId,
                                                                    PermanentChoiceContext.SacrificeCreatureCreateTokensEqualToToughness ctx) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        // Capture effective toughness before removing from battlefield (static bonuses still apply)
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                target.getCard().getName(), ctx.sourceCard().getName());

        // Create X tokens, where X is the sacrificed creature's toughness
        if (toughness > 0) {
            CreateTokenEffect t = ctx.tokenTemplate();
            CreateTokenEffect sized = new CreateTokenEffect(
                    t.primaryType(), toughness, t.tokenName(), t.power(), t.toughness(),
                    t.color(), t.colors(), t.subtypes(), t.keywords(), t.additionalTypes(),
                    t.tappedAndAttacking(), t.tapped(), t.tokenEffects(), t.tokenAbilities(),
                    t.exileAtEndOfCombat(), t.exileAtEndStep(), t.legendary(), t.initialPlusOnePlusOneCounters(),
                    t.grantedKeywordsUntilEndOfTurn());
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ctx.sourceCard(),
                    ctx.controllerId(),
                    ctx.sourceCard().getName() + "'s effect",
                    new ArrayList<>(List.of(sized))
            ));
        }

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleChooseCreatureAsEnter(GameData gameData, UUID chosenCreatureId,
                                             PermanentChoiceContext.ChooseCreatureAsEnter context) {
        Permanent entering = gameQueryService.findPermanentById(gameData, context.enteringPermanentId());
        if (entering == null) {
            throw new IllegalStateException("Entering permanent no longer exists");
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenCreatureId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        entering.setChosenPermanentId(chosenCreatureId);

        String logEntry = entering.getCard().getName() + " chooses " + chosen.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} chooses {} as protected creature", gameData.id,
                entering.getCard().getName(), chosen.getCard().getName());

        battlefieldEntryService.processCreatureETBEffects(gameData, context.controllerId(), context.card(),
                context.targetId(), context.wasCastFromHand(), context.etbMode(), context.kicked());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handlePendingAuraPlacement(GameData gameData, UUID playerId, UUID permanentId) {
        Card auraCard = gameData.interaction.consumePendingAuraCard();
        UUID auraOwnerId = gameData.interaction.consumePendingAuraOwnerId();
        // If an explicit aura owner was set (e.g. Necrotic Plague), use it instead of the chooser
        UUID auraControllerId = auraOwnerId != null ? auraOwnerId : playerId;

        Permanent enchantTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (enchantTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        if (gameData.warpWorldOperation.sourceName != null) {
            gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                    new WarpWorldEnchantmentPlacement(auraControllerId, auraCard, enchantTarget.getId())
            );

            if (!gameData.warpWorldOperation.pendingAuraChoices.isEmpty()) {
                warpWorldService.beginNextPendingWarpWorldAuraChoice(gameData);
                return;
            }
            warpWorldService.placePendingWarpWorldEnchantments(gameData);
            if (!gameData.pendingLibraryBottomReorders.isEmpty()) {
                warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
                return;
            }
            warpWorldService.finalizePendingWarpWorld(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        } else {
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(enchantTarget.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, auraPerm);

            boolean hasControlEffect = auraCard.getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
            if (hasControlEffect) {
                creatureControlService.stealPermanent(gameData, auraControllerId, enchantTarget);
            }

            String playerName = gameData.playerIdToName.get(auraControllerId);
            String logEntry = auraCard.getName() + " enters the battlefield attached to " + enchantTarget.getCard().getName() + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto battlefield attached to {}",
                    gameData.id, playerName, auraCard.getName(), enchantTarget.getCard().getName());
        }

        turnProgressionService.resolveAutoPass(gameData);
    }
}
