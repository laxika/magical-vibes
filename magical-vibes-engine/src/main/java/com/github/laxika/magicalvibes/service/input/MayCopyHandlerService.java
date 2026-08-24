package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyActivatedAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayCopyHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final CloneService cloneService;
    private final PermanentCopierService permanentCopierService;
    private final StateBasedActionService stateBasedActionService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TargetLegalityService targetLegalityService;
    private final TriggerCollectionService triggerCollectionService;
    private final ValidTargetService validTargetService;

    public void handleCopyPermanentOnEnterChoice(GameData gameData, Player player, boolean accepted,
                                                  PendingMayAbility ability, CopyPermanentOnEnterEffect copyEffect) {
        String typeLabel = copyEffect.typeLabel();
        if (accepted) {
            // Collect valid targets (the copying permanent is NOT on the battlefield yet)
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceControllerId(ability.controllerId());
            List<UUID> validIds = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (predicateEvaluationService.matchesPermanentPredicate(p, copyEffect.filter(), filterContext)) {
                        validIds.add(p.getId());
                    }
                }
            }
            playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validIds, "Choose a " + typeLabel + " to copy.");

            String logEntry = player.getUsername() + " accepts — choosing a " + typeLabel + " to copy.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} accepts copy {}", gameData.id, player.getUsername(), typeLabel);
        } else {
            gameData.interaction.clearPermanentChoiceContext();
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to copy a " + typeLabel + ". " , ability.sourceCard(), " enters without copying."));
            log.info("Game {} - {} declines copy {}", gameData.id, player.getUsername(), typeLabel);

            cloneService.completeCloneEntry(gameData, null);
            stateBasedActionService.performStateBasedActions(gameData);

            if (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
                triggerCollectionService.processNextDeathTriggerTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            if (gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
                triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    public void handleCopyCreatureCardInGraveyardOnEnterChoice(
            GameData gameData, Player player, boolean accepted, PendingMayAbility ability,
            CopyCreatureCardInGraveyardOnEnterEffect copyEffect) {
        if (accepted) {
            List<Card> creatureCards = gameData.playerGraveyards.values().stream()
                    .flatMap(List::stream)
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .toList();
            if (!creatureCards.isEmpty()) {
                playerInputService.beginMultiGraveyardChoice(
                        gameData, ability.controllerId(), creatureCards, 1, 1,
                        "Choose a creature card in a graveyard to copy.");
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " accepts — choosing a creature card in a graveyard to copy."));
                return;
            }
        }

        String message = accepted
                ? player.getUsername() + " has no creature card to copy; it enters without copying."
                : player.getUsername() + " declines to copy a creature card from a graveyard. ";
        gameLogService.append(gameData, GameLog.textCardText(message, ability.sourceCard(), " enters without copying."));
        cloneService.completeCloneEntry(gameData, null);
        stateBasedActionService.performStateBasedActions(gameData);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
            triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    public void handleCopySpellRetargetChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            String logEntry = player.getUsername() + " keeps the original targets for the copy.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines to retarget copy", gameData.id, player.getUsername());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Find the copy on the stack
        UUID copyCardId = ability.targetCardId();
        StackEntry copyEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(copyCardId)) {
                copyEntry = se;
                break;
            }
        }

        if (copyEntry == null) {
            log.info("Game {} - Copy no longer on stack for retarget", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card copiedCard = copyEntry.getCard();
        List<UUID> validTargets = new ArrayList<>();

        if (EffectResolution.needsSpellTarget(copiedCard)) {
            // Targets a spell on the stack
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(copyCardId)) continue; // exclude the copy itself
                try {
                    targetLegalityService.validateSpellTargetOnStack(gameData, se.getCard().getId(), copiedCard.getTargetFilter(), copyEntry.getControllerId());
                    validTargets.add(se.getCard().getId());
                } catch (IllegalStateException ignored) {
                    // Invalid target for copied spell filter; skip.
                }
            }
        } else if (EffectResolution.needsTarget(copiedCard)) {
            Zone targetZone = copyEntry.getTargetZone() != null ? copyEntry.getTargetZone() : Zone.BATTLEFIELD;

            List<UUID> candidateTargets = new ArrayList<>(gameData.orderedPlayerIds);
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent p : battlefield) {
                    candidateTargets.add(p.getId());
                }
            }

            for (UUID candidate : candidateTargets) {
                try {
                    targetLegalityService.validateSpellTargeting(
                            gameData,
                            copiedCard,
                            candidate,
                            targetZone,
                            copyEntry.getControllerId()
                    );
                    validTargets.add(candidate);
                } catch (IllegalStateException ignored) {
                    // Candidate is not legal for this copied spell.
                }
            }
        }

        if (validTargets.isEmpty()) {
            String logEntry = "No valid targets available for the copy.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - No valid targets for copy retarget", gameData.id);

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(copyCardId));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for the copy of " + copiedCard.getName() + ".");
    }

    /**
     * "You may choose a new target for the copy" of an activated ability (Rings of Brighthearth).
     * Recomputes the legal targets for the copy on the stack using the copied ability's targeting,
     * then reuses the {@link PermanentChoiceContext.SpellRetarget} flow to apply the chosen target.
     */
    public void handleCopyActivatedAbilityRetargetChoice(GameData gameData, Player player, boolean accepted,
                                                         PendingMayAbility ability, CopyActivatedAbilityRetargetEffect retarget) {
        if (!accepted) {
            String logEntry = player.getUsername() + " keeps the original target for the copy.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines to retarget ability copy", gameData.id, player.getUsername());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID copyCardId = ability.targetCardId();
        StackEntry copyEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(copyCardId)) {
                copyEntry = se;
                break;
            }
        }
        if (copyEntry == null) {
            log.info("Game {} - Ability copy no longer on stack for retarget", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        int permanentIndex = -1;
        List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(ability.controllerId());
        if (controllerBattlefield != null) {
            for (int i = 0; i < controllerBattlefield.size(); i++) {
                if (controllerBattlefield.get(i).getId().equals(retarget.sourcePermanentId())) {
                    permanentIndex = i;
                    break;
                }
            }
        }

        ValidTargetsResponse valid = validTargetService.computeValidTargetsForAbility(
                gameData, copyEntry.getCard(), retarget.ability(), ability.controllerId(), permanentIndex);
        List<UUID> validTargets = new ArrayList<>();
        validTargets.addAll(valid.validPermanentIds());
        validTargets.addAll(valid.validPlayerIds());

        if (validTargets.isEmpty()) {
            gameLogService.append(gameData, GameLog.text("No valid new targets available for the copy."));
            log.info("Game {} - No valid targets for ability copy retarget", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(copyCardId));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for the copy of " + copyEntry.getCard().getName() + "'s ability.");
    }

    /**
     * "You may choose a new target for the copy" of a triggered ability (Strionic Resonator).
     * Builds a synthetic {@link ActivatedAbility} from the copy's snapshotted effects so
     * {@link ValidTargetService#computeValidTargetsForAbility} can recompute legal targets, then
     * reuses the {@link PermanentChoiceContext.SpellRetarget} flow.
     */
    public void handleCopyTriggeredAbilityRetargetChoice(GameData gameData, Player player, boolean accepted,
                                                         PendingMayAbility ability) {
        if (!accepted) {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " keeps the original target for the copy."));
            log.info("Game {} - {} declines to retarget triggered ability copy", gameData.id, player.getUsername());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID copyCardId = ability.targetCardId();
        StackEntry copyEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(copyCardId)) {
                copyEntry = se;
                break;
            }
        }
        if (copyEntry == null) {
            log.info("Game {} - Triggered ability copy no longer on stack for retarget", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        int permanentIndex = -1;
        UUID sourcePermanentId = copyEntry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf == null) continue;
                for (int i = 0; i < bf.size(); i++) {
                    if (bf.get(i).getId().equals(sourcePermanentId)) {
                        permanentIndex = i;
                        break;
                    }
                }
                if (permanentIndex >= 0) break;
            }
        }

        ActivatedAbility synthetic = new ActivatedAbility(
                false, null, List.copyOf(copyEntry.getEffectsToResolve()),
                "copy retarget", copyEntry.getTargetFilter());
        ValidTargetsResponse valid = validTargetService.computeValidTargetsForAbility(
                gameData, copyEntry.getCard(), synthetic, ability.controllerId(), permanentIndex);
        List<UUID> validTargets = new ArrayList<>();
        validTargets.addAll(valid.validPermanentIds());
        validTargets.addAll(valid.validPlayerIds());
        if (valid.validGraveyardCardIds() != null) {
            validTargets.addAll(valid.validGraveyardCardIds());
        }

        if (validTargets.isEmpty()) {
            gameLogService.append(gameData, GameLog.text("No valid new targets available for the copy."));
            log.info("Game {} - No valid targets for triggered ability copy retarget", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(copyCardId));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for the copy of " + copyEntry.getCard().getName()
                        + "'s triggered ability.");
    }

    public void handleRedirectRetargetChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        // Find the target spell on the stack
        UUID spellCardId = ability.targetCardId();
        StackEntry targetSpellEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(spellCardId)) {
                targetSpellEntry = se;
                break;
            }
        }

        String spellName = targetSpellEntry != null ? targetSpellEntry.getCard().getName() : "spell";

        if (!accepted) {
            String logEntry = player.getUsername() + " declines to change targets for " + spellName + ".";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines to redirect spell targets", gameData.id, player.getUsername());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (targetSpellEntry == null) {
            log.info("Game {} - Target spell no longer on stack for redirect", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card spellCard = targetSpellEntry.getCard();
        List<UUID> validTargets = new ArrayList<>();

        if (EffectResolution.needsSpellTarget(spellCard)) {
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCardId)) continue;
                try {
                    targetLegalityService.validateSpellTargetOnStack(gameData, se.getCard().getId(), spellCard.getTargetFilter(), targetSpellEntry.getControllerId());
                    validTargets.add(se.getCard().getId());
                } catch (IllegalStateException ignored) {
                }
            }
        } else if (EffectResolution.needsTarget(spellCard)) {
            Zone targetZone = targetSpellEntry.getTargetZone() != null ? targetSpellEntry.getTargetZone() : Zone.BATTLEFIELD;

            List<UUID> candidateTargets = new ArrayList<>(gameData.orderedPlayerIds);
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    candidateTargets.add(p.getId());
                }
            }

            for (UUID candidate : candidateTargets) {
                try {
                    targetLegalityService.validateSpellTargeting(
                            gameData,
                            spellCard,
                            candidate,
                            targetZone,
                            targetSpellEntry.getControllerId()
                    );
                    validTargets.add(candidate);
                } catch (IllegalStateException ignored) {
                }
            }
        }

        if (validTargets.isEmpty()) {
            
            gameLogService.append(gameData, GameLog.textCardText("No valid new targets for ", spellCard, "."));
            log.info("Game {} - No valid targets for redirect retarget", gameData.id);

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(spellCardId));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for " + spellCard.getName() + ".");
    }

    public void handleBecomeCopyChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();

        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines " , sourceCard, "'s copy ability."));
            log.info("Game {} - {} declines become-copy ability from {}", gameData.id, player.getUsername(), sourceCard.getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Find source permanent by card identity
        Permanent sourcePermanent = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getCard() == sourceCard) {
                    sourcePermanent = p;
                    break;
                }
            }
            if (sourcePermanent != null) break;
        }

        if (sourcePermanent == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(sourceCard, " is no longer on the battlefield."));
            log.info("Game {} - {} become-copy source no longer on battlefield", gameData.id, sourceCard.getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Find target permanent (stored in targetCardId during resolution queueing)
        UUID targetPermId = ability.targetCardId();
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermId);
        if (targetPerm == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(sourceCard, "'s copy target is no longer on the battlefield."));
            log.info("Game {} - {} become-copy target no longer on battlefield", gameData.id, sourceCard.getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Retain the source's copy ability per "except it has this ability".
        BecomeCopyOfTargetCreatureEffect copyEffect = ability.effects().stream()
                .filter(BecomeCopyOfTargetCreatureEffect.class::isInstance)
                .map(BecomeCopyOfTargetCreatureEffect.class::cast)
                .findFirst()
                .orElseThrow();
        // Apply the copy
        String originalName = sourcePermanent.getCard().getName();
        permanentCopierService.applyCloneCopy(sourcePermanent, targetPerm.getCard(), null, null, Set.of(),
                List.of(), copyEffect.copyColor());

        Card copiedCard = sourcePermanent.getCard();
        EffectSlot retainedEffectSlot = copyEffect.retainedEffectSlot();
        for (EffectRegistration registration : sourcePermanent.getOriginalCard()
                .getEffectRegistrations(retainedEffectSlot)) {
            copiedCard.addEffect(retainedEffectSlot, registration.effect(), registration.triggerMode());
        }

        String targetName = targetPerm.getCard().getName();
        
        gameLogService.append(gameData, GameLog.cardThen(sourceCard, "'s copy target is no longer on the battlefield."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, targetName);

        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
