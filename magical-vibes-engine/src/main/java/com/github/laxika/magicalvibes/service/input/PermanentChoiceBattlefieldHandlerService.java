package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageShield;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
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
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler;

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
    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final LifeSupport lifeSupport;
    private final LibrarySearchSupport librarySearchSupport;
    private final MayAbilityTapCostService mayAbilityTapCostService;
    private final TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler sacrificeCreatureCreateTokensIfSubtypeHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.TariffSupport tariffSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.JuxtaposeSupport juxtaposeSupport;

    /**
     * Valleymaker's mana ability: the activating player has chosen {@code chosenPlayerId} as the
     * recipient; add the produced mana to that player's pool. The activating player retains priority
     * (mana abilities don't change priority), so we simply resume the auto-pass loop afterward.
     */
    public void handleManaAbilityAddToChosenPlayer(GameData gameData, UUID chosenPlayerId,
                                                   PermanentChoiceContext.ManaAbilityAddToChosenPlayer context) {
        com.github.laxika.magicalvibes.model.ManaPool pool = gameData.playerManaPools.get(chosenPlayerId);
        if (pool != null && context.amount() > 0) {
            pool.add(context.color(), context.amount());
            if (context.creatureSource()) {
                pool.addCreatureMana(context.color(), context.amount());
            }
        }
        String playerName = gameData.playerIdToName.get(chosenPlayerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " adds " + context.amount() + " "
                + context.color().getCode() + " from " + context.sourceCardName() + "."));

        gameData.priorityPassedBy.clear();
        stateBasedActionService.performStateBasedActions(gameData);
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

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

            if (gameData.hasPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class)) {
                triggerCollectionService.processNextSelfLeavesTriggerTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            // A clone choice can also arise mid-resolution (token-copy effects), so the canonical
            // epilogue must run to resume the parked entry.
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleAttachEquipmentToCreature(GameData gameData, UUID creatureId,
                                                PermanentChoiceContext.AttachEquipmentToCreature context) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, context.equipmentPermanentId());
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (equipment != null && creature != null) {
            gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
            equipment.setAttachedTo(creature.getId());
            // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
            equipment.setTimestamp(gameData.nextTimestamp());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
        }
        // Begun from a library-search resume (Stonehewer Giant) while the search's stack entry is
        // still parked — the canonical epilogue resumes it.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(permanentId);
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", newTarget.getCard(), "."));
        log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

        // Begun mid-resolution (Aura Graft's own spell entry is parked) — canonical epilogue resumes it.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAttachAllAurasToAnotherPermanent(GameData gameData, UUID permanentId,
                                                       PermanentChoiceContext.AttachAllAurasToAnotherPermanent ctx) {
        Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (newTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        for (UUID auraId : ctx.auraPermanentIds()) {
            Permanent aura = gameQueryService.findPermanentById(gameData, auraId);
            if (aura == null) {
                continue;
            }
            gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
            aura.setAttachedTo(permanentId);
            // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
            aura.setTimestamp(gameData.nextTimestamp());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", newTarget.getCard(), "."));
        }

        // A moved control Aura (e.g. Control Magic) grants control of its new host to the Aura's controller.
        creatureControlService.recomputeControl(gameData, newTarget);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleReattachSourceAuraAfterSacrifice(GameData gameData, UUID permanentId,
                                                       PermanentChoiceContext.ReattachSourceAuraAfterSacrifice ctx) {
        Permanent aura = gameQueryService.findPermanentById(gameData, ctx.auraPermanentId());
        if (aura == null) {
            throw new IllegalStateException("Aura permanent no longer exists");
        }

        Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (newTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        // Sacrifice the enchanted permanent, then move the Aura onto the chosen creature or land.
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, ctx.permanentToSacrificeId());
        if (toSacrifice != null) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, ctx.permanentToSacrificeId());
            permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(permanentId);
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", newTarget.getCard(), "."));
        log.info("Game {} - {} reattached to {} after sacrifice", gameData.id,
                aura.getCard().getName(), newTarget.getCard().getName());

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), " is put into the graveyard (legend rule)."));
            log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        // The removals can cascade (a lost anthem making marked damage lethal) and another legend
        // violation may still exist — the epilogue re-runs the CR 704.3 check, which repeats until
        // settled, and resumes any resolution parked by a mid-effect legend check (many normalfx
        // handlers that put legendaries onto the battlefield call checkLegendRule directly).
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreature(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = sacrificeCreature.sacrificingPlayerId();
        Card sacrificedCard = target.getCard();
        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , sacrificedCard, "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, sacrificedCard.getName());

        // Fire the "whenever a player sacrifices a creature" watchers (Thraximundar) — this
        // interactive single-creature-choice path bypasses DestructionSupport.sacrificeAndLog.
        triggerCollectionService.checkAnyCreatureSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);

        // The choice was begun mid-resolution (e.g. Fleshbag Marauder's "each player sacrifices"),
        // so the standard epilogue must run: it resumes the parked resolution entry — otherwise
        // the spell's remaining effects are silently dropped and the park dangles forever.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDestroyChosenCreature(GameData gameData, UUID permanentId,
                                            PermanentChoiceContext.DestroyChosenCreature context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        destructionSupport.tryDestroyAndLog(gameData, target, context.sourceCardName());

        // Begun mid-resolution (opponent/target-player-chooses-creature-to-destroy effects) —
        // same parked-resolution resume requirement as handleSacrificeCreature above.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // "If you do" — sacrifice happened, now search library for a creature card
        librarySearchSupport.searchLibraryForCreatureToHand(gameData, sacrificingPlayerId);

        // When the search awaits input, the library-choice completion owns the epilogue.
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // Each opponent loses life equal to the sacrificed creature's power
        destructionSupport.applyOpponentsLoseLife(gameData, sacrificingPlayerId, power, context.sourceCardName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleForcedCostOrElse(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.ForcedCostOrElse context) {
        destructionSupport.completeForcedCostOrElse(gameData, permanentId, context);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        // Controller gains life equal to the sacrificed creature's toughness
        lifeSupport.applyGainLife(gameData, context.controllerId(), toughness, context.sourceCardName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by bounce effect", gameData.id, target.getCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleBounceOwnPermanentOrSacrificeSelf(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(card, " is exiled by ", source.getCard(), "."));
        log.info("Game {} - {} champions {} (exiled until source leaves)",
                gameData.id, source.getCard().getName(), card.getName());

        gameData.addExileReturnOnPermanentLeave(source.getId(), new PendingExileReturn(card, ownerId));

        permanentRemovalService.removeOrphanedAuras(gameData);

        // "When a creature is championed with this permanent, ..." (e.g. Mistbind Clique).
        List<CardEffect> championedEffects = source.getCard().getEffects(EffectSlot.ON_CHAMPIONED);
        if (championedEffects != null && !championedEffects.isEmpty()) {
            beginChampionedTrigger(gameData, source, context.controllerId(), new ArrayList<>(championedEffects));
            return;
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePutControlledCreatureOnTopOfLibrary(GameData gameData, UUID chosenPermanentId,
                                                          PermanentChoiceContext.PutControlledCreatureOnTopOfLibrary context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        if (permanentRemovalService.removePermanentToLibraryTop(gameData, chosen)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(chosen.getCard(), " is put on top of its owner's library."));
            log.info("Game {} - {} put on top of library (chosen)", gameData.id, chosen.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void beginChampionedTrigger(GameData gameData, Permanent source, UUID controllerId,
                                        List<CardEffect> effects) {
        stateBasedActionService.performStateBasedActions(gameData);

        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData, effects, source.getCard().getTargetFilter(), controllerId,
                source.getCard(), TriggerTargetCollector.Options.END_STEP);

        if (result.validTargets().isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source.getCard(), "'s championed trigger has no valid targets."));
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source.getCard(), "'s championed trigger — choose target player."));
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("All damage ", chosenPermanent.getCard(),
                    " would deal to " + playerName + " is prevented this turn."));
        } else {
            gameData.permanentsPreventedFromDealingDamage.add(permanentId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("All damage ", chosenPermanent.getCard(),
                    " would deal this turn is prevented."));
        }

        log.info("Game {} - {} chose {} as prevented damage source", gameData.id, playerName, sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("The next " + redirectSource.amount() + " damage ",
                chosenPermanent.getCard(),
                " would deal to " + playerName + " or permanents " + playerName + " controls is dealt to another target instead."));
        log.info("Game {} - {} chose {} as redirect damage source (up to {} damage redirected)",
                gameData.id, playerName, chosenPermanent.getCard().getName(), redirectSource.amount());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRedirectCreatureDamageSourceChoice(GameData gameData, UUID permanentId,
                                                         PermanentChoiceContext.RedirectCreatureDamageSourceChoice redirectSource) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        int redirectAmount = redirectSource.nextEventOnly()
                ? CreatureDamageRedirectShield.NEXT_EVENT
                : CreatureDamageRedirectShield.UNLIMITED;
        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                redirectSource.protectedCreatureId(), permanentId,
                redirectAmount, redirectSource.redirectTargetId()));

        Permanent protectedPerm = gameQueryService.findPermanentById(gameData, redirectSource.protectedCreatureId());
        String protectedName = protectedPerm != null ? protectedPerm.getCard().getName() : "target creature";
        // The redirect target may be a player (Jade Monolith) or a permanent (Oracle's Attendants).
        String redirectName = gameData.playerIdToName.get(redirectSource.redirectTargetId());
        if (redirectName == null) {
            Permanent redirectPerm = gameQueryService.findPermanentById(gameData, redirectSource.redirectTargetId());
            redirectName = redirectPerm != null ? redirectPerm.getCard().getName() : "another creature";
        }
        String prefix = redirectSource.nextEventOnly() ? "The next time " : "All damage ";
        String suffix = redirectSource.nextEventOnly()
                ? " would deal damage to " + protectedName + " this turn, that damage is dealt to " + redirectName + " instead."
                : " would deal to " + protectedName + " this turn is dealt to " + redirectName + " instead.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(prefix, chosenPermanent.getCard(), suffix));
        log.info("Game {} - {} chose {} as creature damage redirect source", gameData.id,
                gameData.playerIdToName.get(redirectSource.controllerId()), chosenPermanent.getCard().getName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text("The next " + ctx.amount() + " damage ").card(chosenPermanent.getCard()).text(" would deal to " + targetName + " is prevented.").build());
        log.info("Game {} - Chose {} as damage source, preventing up to {} damage to {}",
                gameData.id, chosenPermanent.getCard().getName(), ctx.amount(), targetName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreventNextDamageFromSourceChoice(GameData gameData, UUID permanentId,
                                                        PermanentChoiceContext.PreventNextDamageFromSourceChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID controllerId = ctx.controllerId();
        boolean gainLife = ctx.gainLife();
        gameData.playerSourceNextDamageShields.add(new PlayerSourceNextDamageShield(controllerId, permanentId, gainLife));

        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = chosenPermanent.getCard().getName();
        String logEntry = "The next time " + sourceName + " would deal damage to " + playerName
                + " this turn, it is prevented" + (gainLife ? " and " + playerName + " gains that much life." : ".");
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chose {} as next-damage prevention source", gameData.id, playerName, sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleEyeForAnEyeSourceChoice(GameData gameData, UUID permanentId,
                                              PermanentChoiceContext.EyeForAnEyeSourceChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID controllerId = ctx.controllerId();
        gameData.eyeForAnEyeShields.add(new com.github.laxika.magicalvibes.model.EyeForAnEyeShield(
                controllerId, permanentId, ctx.eyeCard(), controllerId));

        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = chosenPermanent.getCard().getName();
        String logEntry = "The next time " + sourceName + " would deal damage to " + playerName
                + " this turn, that much damage is also dealt to " + sourceName + "'s controller.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chose {} as Eye for an Eye reflection source", gameData.id, playerName, sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreventNextDamageFromSourceToAnyTargetChoice(GameData gameData, UUID permanentId,
                                                                   PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice ctx) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        gameData.sourceNextDamageToAnyTargetShields.add(permanentId);

        String sourceName = chosenPermanent.getCard().getName();
        String logEntry = "The next time " + sourceName + " would deal damage to any target this turn, it is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chose {} as Sanctum Guardian next-damage prevention source", gameData.id,
                gameData.playerIdToName.get(ctx.controllerId()), sourceName);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeArtifactForDividedDamage(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd) {
        Permanent artifactToSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (artifactToSacrifice == null) {
            throw new IllegalStateException("Artifact permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, artifactToSacrifice);

        String playerName = gameData.playerIdToName.get(sadd.controllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , artifactToSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for divided damage", gameData.id, playerName, artifactToSacrifice.getCard().getName());

        damageSupport.dealDividedDamageToAnyTargets(
                gameData, sadd.sourceCard(), sadd.controllerId(), sadd.damageAssignments());

        gameData.pendingETBDamageAssignments = Map.of();

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeAnotherCreatureDealPowerDamage(GameData gameData, UUID permanentId,
                                                              PermanentChoiceContext.SacrificeAnotherCreatureDealPowerDamage ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        // Capture effective power before removing from battlefield (static bonuses still apply;
        // CR 510.1a clamps negative power to 0).
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, toSacrifice));

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices ", toSacrifice.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                toSacrifice.getCard().getName(), ctx.sourceCard().getName());

        // The source deals damage equal to the sacrificed creature's power to the chosen any-target.
        // Reuses the divided-damage helper with a single assignment; it finds the source permanent by
        // card, so the damage is dealt by the entering creature (honouring protection / prevention).
        if (power > 0 && ctx.targetId() != null) {
            damageSupport.dealDividedDamageToAnyTargets(gameData, ctx.sourceCard(), ctx.controllerId(),
                    Map.of(ctx.targetId(), power));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificePermanentThen(GameData gameData, UUID permanentId,
                                              PermanentChoiceContext.SacrificePermanentThen ctx) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , toSacrifice.getCard(), "."));
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

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                target.getCard().getName(), ctx.sourceCard().getName());

        // Create X tokens, where X is the sacrificed creature's toughness
        if (toughness > 0) {
            CreateTokenEffect sized = ctx.tokenTemplate().withAmount(toughness);
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ctx.sourceCard(),
                    ctx.controllerId(),
                    ctx.sourceCard().getName() + "'s effect",
                    new ArrayList<>(List.of(sized))
            ));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureCreateSizedTokenEqualToPower(GameData gameData, UUID permanentId,
                                                                    PermanentChoiceContext.SacrificeCreatureCreateSizedTokenEqualToPower ctx) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        // Capture effective power before removing from battlefield (static bonuses still apply)
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, target));

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " sacrifices " , target.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                target.getCard().getName(), ctx.sourceCard().getName());

        // Create one token whose power and toughness are each equal to the sacrificed creature's power
        CreateTokenEffect t = ctx.tokenTemplate();
        CreateTokenEffect sized = new CreateTokenEffect(
                t.primaryType(), 1, t.tokenName(), power, power,
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

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeCreatureCreateTokensIfSubtype(GameData gameData, UUID permanentId,
                                                             PermanentChoiceContext.SacrificeCreatureCreateTokensIfSubtype ctx) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        sacrificeCreatureCreateTokensIfSubtypeHandler.sacrificeAndMaybeCreateTokens(
                gameData, ctx.sacrificingPlayerId(), target, ctx.requiredSubtype(),
                ctx.tokenTemplate(), ctx.sourceCard().getSetCode());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleTariffTieBreak(GameData gameData, UUID permanentId,
                                     PermanentChoiceContext.TariffTieBreak context) {
        tariffSupport.handleTieBreakChosen(gameData, permanentId, context);
    }

    public void handleJuxtaposeTieBreak(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.JuxtaposeTieBreak context) {
        juxtaposeSupport.handleTieBreakChosen(gameData, permanentId, context);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entering.getCard(), " chooses ", chosen.getCard(), "."));
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
                creatureControlService.applyControlEffect(gameData, auraControllerId, enchantTarget,
                        new ControlEnchantedCreatureEffect(), EffectDuration.WHILE_ATTACHED,
                        auraPerm.getId(), auraCard.getName());
            }

            String playerName = gameData.playerIdToName.get(auraControllerId);
            String logEntry = auraCard.getName() + " enters the battlefield attached to " + enchantTarget.getCard().getName() + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(auraCard).text(" enters the battlefield attached to ").card(enchantTarget.getCard()).text(" under " + playerName + "'s control.").build());
            log.info("Game {} - {} puts {} onto battlefield attached to {}",
                    gameData.id, playerName, auraCard.getName(), enchantTarget.getCard().getName());
        }

        // Aura placements are begun by normalfx handlers mid-resolution (including Warp World,
        // whose own entry is parked while the choices run) — the epilogue resumes the parked entry.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
