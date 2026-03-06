package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.combat.DamageResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
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
public class PermanentChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DeathTriggerService deathTriggerService;
    private final CloneService cloneService;
    private final WarpWorldService warpWorldService;
    private final GameBroadcastService gameBroadcastService;
    private final AbilityActivationService abilityActivationService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final StateBasedActionService stateBasedActionService;
    private final TriggerCollectionService triggerCollectionService;
    private final CreatureControlService creatureControlService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final DamageResolutionService damageResolutionService;

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)) {
            throw new IllegalStateException("Not awaiting permanent choice");
        }
        InteractionContext.PermanentChoice permanentChoice = gameData.interaction.permanentChoiceContextView();
        if (permanentChoice == null || !player.getId().equals(permanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = permanentChoice.validIds();

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearPermanentChoice();

        if (!validIds.contains(permanentId)) {
            throw new IllegalStateException("Invalid permanent: " + permanentId);
        }

        PermanentChoiceContext context = permanentChoice.context();
        gameData.interaction.clearPermanentChoiceContext();

        if (context instanceof PermanentChoiceContext.CloneCopy) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, permanentId);
            if (targetPerm == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            cloneService.completeCloneEntry(gameData, permanentId);

            // If no legend rule or other awaiting input pending, do SBA + auto-pass
            if (!gameData.interaction.isAwaitingInput()) {
                stateBasedActionService.performStateBasedActions(gameData);

                if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
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
        } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
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
        } else if (context instanceof PermanentChoiceContext.LegendRule legendRule) {
            // Legend rule: keep chosen permanent, move all others with the same name to graveyard
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            List<Permanent> toRemove = new ArrayList<>();
            for (Permanent perm : battlefield) {
                if (perm.getCard().getName().equals(legendRule.cardName()) && !perm.getId().equals(permanentId)) {
                    toRemove.add(perm);
                }
            }
            for (Permanent perm : toRemove) {
                boolean wasCreature = gameQueryService.isCreature(gameData, perm);
                battlefield.remove(perm);
                boolean wentToGraveyard = gameHelper.addCardToGraveyard(gameData, playerId, perm.getOriginalCard(), Zone.BATTLEFIELD);
                if (wentToGraveyard) {
                    deathTriggerService.collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature, perm);
                    if (wasCreature) {
                        deathTriggerService.checkAllyCreatureDeathTriggers(gameData, playerId);
                    }
                }
                String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
            }

            permanentRemovalService.removeOrphanedAuras(gameData);

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
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
        } else if (context instanceof PermanentChoiceContext.ActivatedAbilityCostChoice costChoice) {
            abilityActivationService.completeActivatedAbilityCostChoice(gameData, player, costChoice, permanentId);
        } else if (context instanceof PermanentChoiceContext.BounceCreature bounceCreature) {
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
        } else if (context instanceof PermanentChoiceContext.BounceOwnPermanentOrSacrificeSelf bounceOrSac) {
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

            stateBasedActionService.performStateBasedActions(gameData);

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.SpellRetarget retarget) {
            StackEntry targetSpell = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(retarget.spellCardId())) {
                    targetSpell = se;
                    break;
                }
            }
            if (targetSpell == null) {
                log.info("Game {} - Target spell no longer on stack for retarget", gameData.id);
            } else {
                targetSpell.setTargetPermanentId(permanentId);
                String spellName = targetSpell.isCopy()
                        ? "Copy of " + targetSpell.getCard().getName()
                        : targetSpell.getCard().getName();
                String targetName = getTargetDisplayName(gameData, permanentId);
                String logMsg = spellName + " now targets " + targetName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - {} retargeted to {}", gameData.id, spellName, targetName);

                // Check becomes-target-of-spell triggers for the new target (e.g. Livewire Lash)
                triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, targetSpell);
                if (gameData.interaction.isAwaitingInput()) return;
            }

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.SpellTargetTriggerAnyTarget stt) {
            // Spell-target trigger targeting any target (e.g. Livewire Lash)
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    stt.sourceCard(),
                    stt.controllerId(),
                    stt.sourceCard().getName() + "'s ability",
                    new ArrayList<>(stt.effects())
            );
            entry.setTargetPermanentId(permanentId);
            gameData.stack.add(entry);

            String targetName = getTargetDisplayName(gameData, permanentId);
            String logEntry = stt.sourceCard().getName() + "'s triggered ability targets " + targetName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} spell-target trigger targets {}", gameData.id, stt.sourceCard().getName(), targetName);

            // Process more pending spell target triggers
            if (!gameData.pendingSpellTargetTriggers.isEmpty()) {
                triggerCollectionService.processNextSpellTargetTrigger(gameData);
                return;
            }

            // Process pending may abilities
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.DiscardTriggerAnyTarget dtt) {
            // Discard self-trigger targeting any target (creature or player)
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    dtt.discardedCard(),
                    dtt.controllerId(),
                    dtt.discardedCard().getName() + "'s ability",
                    new ArrayList<>(dtt.effects())
            );
            entry.setTargetPermanentId(permanentId);
            gameData.stack.add(entry);

            String targetName = getTargetDisplayName(gameData, permanentId);
            String logEntry = dtt.discardedCard().getName() + "'s discard trigger targets " + targetName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discard trigger targets {}", gameData.id, dtt.discardedCard().getName(), targetName);

            // Process more pending discard self triggers
            if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
                triggerCollectionService.processNextDiscardSelfTrigger(gameData);
                return;
            }

            // Process pending death trigger targets
            if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                triggerCollectionService.processNextDeathTriggerTarget(gameData);
                return;
            }

            // Process pending may abilities
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.DeathTriggerTarget dtt) {
            // Create the triggered ability stack entry with the chosen target (permanent or player)
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    dtt.dyingCard(),
                    dtt.controllerId(),
                    dtt.dyingCard().getName() + "'s ability",
                    new ArrayList<>(dtt.effects())
            );
            entry.setTargetPermanentId(permanentId);
            gameData.stack.add(entry);

            String targetName = getTargetDisplayName(gameData, permanentId);
            String logEntry = dtt.dyingCard().getName() + "'s death trigger targets " + targetName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} death trigger targets {}", gameData.id, dtt.dyingCard().getName(), targetName);

            // Process more pending death trigger targets
            if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                triggerCollectionService.processNextDeathTriggerTarget(gameData);
                return;
            }

            // Process pending may abilities
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.PreventDamageSourceChoice preventSource) {
            Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (chosenPermanent == null) {
                throw new IllegalStateException("Chosen permanent no longer exists");
            }

            UUID controllerId = preventSource.controllerId();
            gameData.playerSourceDamagePreventionIds
                    .computeIfAbsent(controllerId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(permanentId);

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = "All damage " + chosenPermanent.getCard().getName() + " would deal to " + playerName + " is prevented this turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} chose {} as prevented damage source", gameData.id, playerName, chosenPermanent.getCard().getName());

            stateBasedActionService.performStateBasedActions(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.MayAbilityTriggerTarget mat) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

            if (target != null || isPlayerTarget) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        mat.sourceCard(),
                        mat.controllerId(),
                        mat.sourceCard().getName() + "'s ability",
                        new ArrayList<>(mat.effects())
                );
                entry.setTargetPermanentId(permanentId);
                gameData.stack.add(entry);

                if (isPlayerTarget) {
                    String playerName = gameData.playerIdToName.get(permanentId);
                    String logEntry = mat.sourceCard().getName() + "'s ability targets " + playerName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} may-ability trigger targets player {}", gameData.id, mat.sourceCard().getName(), playerName);
                } else {
                    String logEntry = mat.sourceCard().getName() + "'s ability targets " + target.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} may-ability trigger targets {}", gameData.id, mat.sourceCard().getName(), target.getCard().getName());
                }
            } else {
                String logEntry = mat.sourceCard().getName() + "'s ability has no valid target.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} may-ability trigger target no longer exists", gameData.id, mat.sourceCard().getName());
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd) {
            Permanent artifactToSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
            if (artifactToSacrifice == null) {
                throw new IllegalStateException("Artifact permanent no longer exists");
            }

            permanentRemovalService.removePermanentToGraveyard(gameData, artifactToSacrifice);

            String playerName = gameData.playerIdToName.get(sadd.controllerId());
            String logEntry = playerName + " sacrifices " + artifactToSacrifice.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} sacrifices {} for divided damage", gameData.id, playerName, artifactToSacrifice.getCard().getName());

            damageResolutionService.dealDividedDamageToAnyTargets(
                    gameData, sadd.sourceCard(), sadd.controllerId(), sadd.damageAssignments());

            gameData.pendingETBDamageAssignments = java.util.Map.of();

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
        } else if (context instanceof PermanentChoiceContext.LibraryCastSpellTarget lct) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

            if (target != null || isPlayerTarget) {
                StackEntry entry = new StackEntry(
                        lct.spellType(),
                        lct.cardToCast(),
                        lct.controllerId(),
                        lct.cardToCast().getName(),
                        new ArrayList<>(lct.spellEffects()),
                        0,
                        permanentId,
                        null
                );
                gameData.stack.add(entry);

                gameData.spellsCastThisTurn.merge(lct.controllerId(), 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                String targetName = isPlayerTarget
                        ? gameData.playerIdToName.get(permanentId)
                        : target.getCard().getName();
                String logEntry = lct.cardToCast().getName() + " targets " + targetName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} cast-from-library targets {}", gameData.id, lct.cardToCast().getName(), targetName);

                triggerCollectionService.checkSpellCastTriggers(gameData, lct.cardToCast(), lct.controllerId(), false);
                triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
            } else {
                // Target no longer valid — put card into graveyard
                gameHelper.addCardToGraveyard(gameData, lct.controllerId(), lct.cardToCast());
                String logEntry = lct.cardToCast().getName() + "'s target is no longer valid. It is put into the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} cast-from-library target no longer exists", gameData.id, lct.cardToCast().getName());
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.ExileCastSpellTarget ect) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

            if (target != null || isPlayerTarget) {
                StackEntry entry = new StackEntry(
                        ect.spellType(),
                        ect.cardToCast(),
                        ect.controllerId(),
                        ect.cardToCast().getName(),
                        new ArrayList<>(ect.spellEffects()),
                        0,
                        permanentId,
                        null
                );
                gameData.stack.add(entry);

                gameData.spellsCastThisTurn.merge(ect.controllerId(), 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                String targetName = isPlayerTarget
                        ? gameData.playerIdToName.get(permanentId)
                        : target.getCard().getName();
                String logEntry = ect.cardToCast().getName() + " targets " + targetName + " (Knowledge Pool).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} cast-from-exile targets {}", gameData.id, ect.cardToCast().getName(), targetName);

                triggerCollectionService.checkSpellCastTriggers(gameData, ect.cardToCast(), ect.controllerId(), false);
                triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
            } else {
                // Target no longer valid — put card into graveyard
                gameHelper.addCardToGraveyard(gameData, ect.controllerId(), ect.cardToCast());
                String logEntry = ect.cardToCast().getName() + "'s target is no longer valid. It is put into the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} cast-from-exile target no longer exists", gameData.id, ect.cardToCast().getName());
            }

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.GraveyardCastSpellTarget gct) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

            if (target != null || isPlayerTarget) {
                StackEntry entry = new StackEntry(
                        gct.spellType(),
                        gct.cardToCast(),
                        gct.controllerId(),
                        gct.cardToCast().getName(),
                        new ArrayList<>(gct.spellEffects()),
                        0,
                        permanentId,
                        null
                );
                gameData.stack.add(entry);

                gameData.spellsCastThisTurn.merge(gct.controllerId(), 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                String targetName = isPlayerTarget
                        ? gameData.playerIdToName.get(permanentId)
                        : target.getCard().getName();
                String logEntry = gct.cardToCast().getName() + " targets " + targetName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} cast-from-graveyard targets {}", gameData.id, gct.cardToCast().getName(), targetName);

                triggerCollectionService.checkSpellCastTriggers(gameData, gct.cardToCast(), gct.controllerId(), false);
                triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
            } else {
                // Target no longer valid — put card into graveyard
                gameHelper.addCardToGraveyard(gameData, gct.controllerId(), gct.cardToCast());
                String logEntry = gct.cardToCast().getName() + "'s target is no longer valid. It is put into the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} cast-from-graveyard target no longer exists", gameData.id, gct.cardToCast().getName());
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.AttackTriggerTarget att) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        att.sourceCard(),
                        att.controllerId(),
                        att.sourceCard().getName() + "'s ability",
                        new ArrayList<>(att.effects()),
                        null,
                        att.sourcePermanentId()
                );
                entry.setTargetPermanentId(permanentId);
                gameData.stack.add(entry);

                String logEntry = att.sourceCard().getName() + "'s ability targets " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} attack trigger targets {}", gameData.id, att.sourceCard().getName(), target.getCard().getName());
            } else {
                String logEntry = att.sourceCard().getName() + "'s ability has no valid target.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} attack trigger target no longer exists", gameData.id, att.sourceCard().getName());
            }

            // Process remaining pending attack triggers
            if (!gameData.pendingAttackTriggerTargets.isEmpty()) {
                triggerCollectionService.processNextAttackTriggerTarget(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.EmblemTriggerTarget ett) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ett.sourceCard(),
                        ett.controllerId(),
                        ett.emblemDescription() + "'s ability",
                        new ArrayList<>(ett.effects())
                );
                entry.setTargetPermanentId(permanentId);
                gameData.stack.add(entry);

                String logEntry = ett.emblemDescription() + "'s ability targets " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} emblem trigger targets {}", gameData.id, ett.emblemDescription(), target.getCard().getName());
            } else {
                String logEntry = ett.emblemDescription() + "'s ability has no valid target.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} emblem trigger target no longer exists", gameData.id, ett.emblemDescription());
            }

            // Process remaining pending emblem triggers
            if (!gameData.pendingEmblemTriggerTargets.isEmpty()) {
                triggerCollectionService.processNextEmblemTriggerTarget(gameData);
                return;
            }

            // Process pending may abilities
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.UpkeepCopyTriggerTarget uct) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        uct.sourceCard(),
                        uct.controllerId(),
                        uct.sourceCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new BecomeCopyOfTargetCreatureEffect())),
                        null,
                        uct.sourcePermanentId()
                );
                entry.setTargetPermanentId(permanentId);
                gameData.stack.add(entry);

                String logEntry = uct.sourceCard().getName() + "'s ability targets " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} upkeep copy trigger targets {}", gameData.id, uct.sourceCard().getName(), target.getCard().getName());
            } else {
                String logEntry = uct.sourceCard().getName() + "'s ability has no valid target.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} upkeep copy trigger target no longer exists", gameData.id, uct.sourceCard().getName());
            }

            if (!gameData.pendingUpkeepCopyTargets.isEmpty()) {
                turnProgressionService.processNextUpkeepCopyTarget(gameData);
                return;
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (gameData.interaction.pendingAuraCard() != null) {
            Card auraCard = gameData.interaction.consumePendingAuraCard();

            Permanent enchantTarget = gameQueryService.findPermanentById(gameData, permanentId);
            if (enchantTarget == null) {
                throw new IllegalStateException("Target permanent no longer exists");
            }

            if (gameData.warpWorldOperation.sourceName != null) {
                gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                        new com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement(playerId, auraCard, enchantTarget.getId())
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
                // Create Aura permanent attached to the chosen permanent, under controller's control
                Permanent auraPerm = new Permanent(auraCard);
                auraPerm.setAttachedTo(enchantTarget.getId());
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, auraPerm);

                boolean hasControlEffect = auraCard.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                if (hasControlEffect) {
                    creatureControlService.stealPermanent(gameData, playerId, enchantTarget);
                }

                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = auraCard.getName() + " enters the battlefield attached to " + enchantTarget.getCard().getName() + " under " + playerName + "'s control.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} puts {} onto battlefield attached to {}",
                        gameData.id, playerName, auraCard.getName(), enchantTarget.getCard().getName());
            }

            turnProgressionService.resolveAutoPass(gameData);
        } else {
            throw new IllegalStateException("No pending permanent choice context");
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.MULTI_PERMANENT_CHOICE)) {
            throw new IllegalStateException("Not awaiting multi-permanent choice");
        }
        InteractionContext.MultiPermanentChoice multiPermanentChoice = gameData.interaction.multiPermanentChoiceContext();
        if (multiPermanentChoice == null || !player.getId().equals(multiPermanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = multiPermanentChoice.validIds();
        int maxCount = multiPermanentChoice.maxCount();

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearMultiPermanentChoice();

        if (permanentIds == null) {
            permanentIds = List.of();
        }

        if (permanentIds.size() > maxCount) {
            throw new IllegalStateException("Too many permanents selected: " + permanentIds.size() + " > " + maxCount);
        }

        // Validate no duplicates
        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }

        for (UUID permId : permanentIds) {
            if (!validIds.contains(permId)) {
                throw new IllegalStateException("Invalid permanent: " + permId);
            }
        }

        if (gameData.pendingSacrificeSelfToDestroySourceId != null) {
            UUID sourcePermId = gameData.pendingSacrificeSelfToDestroySourceId;
            gameData.pendingSacrificeSelfToDestroySourceId = null;

            if (permanentIds.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to sacrifice.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                // Sacrifice source creature
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
                if (source != null) {
                    if (permanentRemovalService.removePermanentToGraveyard(gameData, source)) {
                        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId);
                        String logEntry = source.getCard().getName() + " is sacrificed.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} sacrificed for combat damage trigger", gameData.id, source.getCard().getName());

                        // Destroy target creature
                        UUID chosenPermId = permanentIds.getFirst();
                        Permanent target = gameQueryService.findPermanentById(gameData, chosenPermId);
                        if (target != null) {
                            if (permanentRemovalService.tryDestroyPermanent(gameData, target)) {
                                logEntry = target.getCard().getName() + " is destroyed.";
                                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                                log.info("Game {} - {} destroyed by sacrifice trigger", gameData.id, target.getCard().getName());
                            }
                        }
                    }

                    permanentRemovalService.removeOrphanedAuras(gameData);
                } else {
                    String logEntry = "Source creature no longer exists — sacrifice trigger fizzles.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }

            stateBasedActionService.performStateBasedActions(gameData);

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (gameData.pendingSacrificeAttackingCreature) {
            gameData.pendingSacrificeAttackingCreature = false;

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
                    String logEntry = ownerName + " sacrifices " + creature.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, creature.getCard().getName());
                }
            }

            stateBasedActionService.performStateBasedActions(gameData);

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (gameData.pendingCombatDamageBounceTargetPlayerId != null) {
            UUID targetPlayerId = gameData.pendingCombatDamageBounceTargetPlayerId;
            gameData.pendingCombatDamageBounceTargetPlayerId = null;

            if (permanentIds.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to return any permanents.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} bounced {} permanents", gameData.id, gameData.playerIdToName.get(playerId), bouncedNames.size());
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            turnProgressionService.advanceStep(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (gameData.pendingAwakeningCounterPlacement) {
            gameData.pendingAwakeningCounterPlacement = false;

            if (permanentIds.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to put awakening counters on any lands.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                List<String> awakenedNames = new ArrayList<>();
                for (UUID permId : permanentIds) {
                    Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                    if (perm != null) {
                        perm.setAwakeningCounters(perm.getAwakeningCounters() + 1);
                        awakenedNames.add(perm.getCard().getName());
                    }
                }

                if (!awakenedNames.isEmpty()) {
                    String logEntry = String.join(", ", awakenedNames)
                            + (awakenedNames.size() == 1 ? " receives" : " receive")
                            + " an awakening counter and "
                            + (awakenedNames.size() == 1 ? "becomes an" : "become")
                            + " 8/8 green Elemental creature"
                            + (awakenedNames.size() == 1 ? "." : "s.");
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - Awakening counters placed on {} lands", gameData.id, awakenedNames.size());
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            turnProgressionService.advanceStep(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (gameData.pendingProliferateCount > 0) {
            gameData.pendingProliferateCount--;

            if (permanentIds.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to proliferate any permanents.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                List<String> proliferatedNames = new ArrayList<>();
                for (UUID permId : permanentIds) {
                    Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                    if (perm != null) {
                        if (!gameQueryService.cantHaveCounters(gameData, perm)) {
                            if (perm.getPlusOnePlusOneCounters() > 0) {
                                perm.setPlusOnePlusOneCounters(perm.getPlusOnePlusOneCounters() + 1);
                            }
                            if (perm.getMinusOneMinusOneCounters() > 0) {
                                perm.setMinusOneMinusOneCounters(perm.getMinusOneMinusOneCounters() + 1);
                            }
                            if (perm.getLoyaltyCounters() > 0) {
                                perm.setLoyaltyCounters(perm.getLoyaltyCounters() + 1);
                            }
                            if (perm.getAwakeningCounters() > 0) {
                                perm.setAwakeningCounters(perm.getAwakeningCounters() + 1);
                            }
                        }
                        proliferatedNames.add(perm.getCard().getName());
                    }
                }

                if (!proliferatedNames.isEmpty()) {
                    String logEntry = "Proliferate adds counters to " + String.join(", ", proliferatedNames) + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - Proliferated {} permanents", gameData.id, proliferatedNames.size());
                }
            }

            // More proliferates remaining (e.g. "proliferate, then proliferate again")
            // Per MTG Rule 704.3, SBA are not checked during ability resolution,
            // so defer SBA until all proliferates are done.
            if (gameData.pendingProliferateCount > 0) {
                List<UUID> eligiblePermanentIds = new ArrayList<>();
                gameData.forEachPermanent((pid, p) -> {
                    if (p.getPlusOnePlusOneCounters() > 0
                            || p.getMinusOneMinusOneCounters() > 0
                            || p.getLoyaltyCounters() > 0
                            || p.getAwakeningCounters() > 0) {
                        eligiblePermanentIds.add(p.getId());
                    }
                });
                if (eligiblePermanentIds.isEmpty()) {
                    gameData.pendingProliferateCount = 0;
                    String logEntry = "Proliferate: no permanents with counters to choose.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                } else {
                    playerInputService.beginMultiPermanentChoice(gameData, playerId, eligiblePermanentIds,
                            eligiblePermanentIds.size(), "Proliferate: Choose permanents to add counters to.");
                    return;
                }
            }

            // All proliferates done — now check SBA
            stateBasedActionService.performStateBasedActions(gameData);

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            // Resume resolving remaining effects on the same spell/ability (e.g. "Proliferate. Draw a card.")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            gameBroadcastService.broadcastGameState(gameData);
        } else if (gameData.pendingTapSubtypeBoostSourcePermanentId != null) {
            UUID sourcePermanentId = gameData.pendingTapSubtypeBoostSourcePermanentId;
            gameData.pendingTapSubtypeBoostSourcePermanentId = null;

            int count = permanentIds.size();

            if (count == 0) {
                String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to tap any Myr.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                // Tap the chosen permanents
                List<String> tappedNames = new ArrayList<>();
                for (UUID permId : permanentIds) {
                    Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                    if (perm != null) {
                        perm.tap();
                        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                        tappedNames.add(perm.getCard().getName());
                    }
                }

                if (!tappedNames.isEmpty()) {
                    String tapLog = gameData.playerIdToName.get(playerId) + " taps " + tappedNames.size()
                            + " Myr: " + String.join(", ", tappedNames) + ".";
                    gameBroadcastService.logAndBroadcast(gameData, tapLog);
                    log.info("Game {} - {} taps {} Myr for attack trigger", gameData.id,
                            gameData.playerIdToName.get(playerId), tappedNames.size());
                }

                // Boost source permanent +X/+0 (only if still on battlefield)
                Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                String sourceName;
                if (sourcePermanent != null) {
                    sourcePermanent.setPowerModifier(sourcePermanent.getPowerModifier() + count);
                    sourceName = sourcePermanent.getCard().getName();
                    String boostLog = sourceName + " gets +" + count + "/+0 until end of turn.";
                    gameBroadcastService.logAndBroadcast(gameData, boostLog);
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
                    gameBroadcastService.logAndBroadcast(gameData, sourceName + "'s damage to " + defenderName + " is prevented.");
                } else {
                    // Apply damage multiplier (DoubleDamageEffect)
                    int damage = count;
                    final int[] multiplier = {1};
                    gameData.forEachPermanent((pid, p) -> {
                        for (com.github.laxika.magicalvibes.model.effect.CardEffect e : p.getCard().getEffects(EffectSlot.STATIC)) {
                            if (e instanceof com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect) {
                                multiplier[0] *= 2;
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
                        if (hasInfect) {
                            int currentPoison = gameData.playerPoisonCounters.getOrDefault(defendingPlayerId, 0);
                            gameData.playerPoisonCounters.put(defendingPlayerId, currentPoison + damage);
                            gameBroadcastService.logAndBroadcast(gameData, defenderName + " gets "
                                    + damage + " poison counter" + (damage > 1 ? "s" : "") + " from " + sourceName + ".");
                        } else if (!gameQueryService.canPlayerLifeChange(gameData, defendingPlayerId)) {
                            gameBroadcastService.logAndBroadcast(gameData,
                                    defenderName + "'s life total can't change.");
                        } else {
                            int currentLife = gameData.playerLifeTotals.getOrDefault(defendingPlayerId, 20);
                            gameData.playerLifeTotals.put(defendingPlayerId, currentLife - damage);
                            gameBroadcastService.logAndBroadcast(gameData, sourceName + " deals "
                                    + damage + " damage to " + defenderName + ".");
                        }
                    }
                }
            }

            stateBasedActionService.performStateBasedActions(gameData);

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else {
            throw new IllegalStateException("No pending multi-permanent choice context");
        }
    }

    private String getTargetDisplayName(GameData gameData, UUID targetId) {
        // Check if it's a player
        String playerName = gameData.playerIdToName.get(targetId);
        if (playerName != null) return playerName;

        // Check stack entries
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetId)) return se.getCard().getName();
        }

        // Check battlefield permanents
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(targetId)) return p.getCard().getName();
            }
        }

        // Check graveyards
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        if (graveyardCard != null) return graveyardCard.getName();

        return targetId.toString();
    }
}


