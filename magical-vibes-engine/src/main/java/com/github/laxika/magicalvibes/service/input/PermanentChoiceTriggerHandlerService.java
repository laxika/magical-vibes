package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OptionalTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DemonstrateEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySpellForAnotherOpponentPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySpellForEachOtherControlledCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LeastToughnessDamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.MakeTargetCreatureCantBeBlockedByMostLifePlayerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealUntilCardPredicateRestOnBottomRandomEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TheMasterGallifreysEndEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseAttackTargetsForCreatedTokensEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TokenCopySupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles permanent choice contexts related to triggered ability targeting.
 *
 * <p>Covers spell-target triggers, discard triggers, death triggers,
 * may-ability triggers, attack triggers, emblem triggers, and upkeep copy triggers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceTriggerHandlerService {

    private final GameQueryService gameQueryService;
    private final AttackLegalityService attackLegalityService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final com.github.laxika.magicalvibes.service.target.ValidTargetService validTargetService;
    private final CreatureControlService creatureControlService;
    private final LeastToughnessDamageSupport leastToughnessDamageSupport;
    private final CopySpellForEachOtherControlledCreatureEffectHandler copySpellHandler;
    private final CopySpellForAnotherOpponentPermanentEffectHandler copySpellForAnotherOpponentPermanentHandler;
    private final DemonstrateEffectHandler demonstrateEffectHandler;
    private final TokenCopySupport tokenCopySupport;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealUntilCardHandler;
    private final MakeTargetCreatureCantBeBlockedByMostLifePlayerEffectHandler blackGateHandler;
    private final TheMasterGallifreysEndEffectHandler theMasterHandler;
    private final ChooseAttackTargetsForCreatedTokensEffectHandler createdTokenAttackTargetsHandler;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.target.TargetLegalityService targetLegalityService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

    /**
     * The message for a target answer that wasn't among the offered choices: when the answer is a player
     * the trigger's source can't target, the reason (protection, hexproof, ...) is reported. Otherwise the
     * declared target filter's own error message, when it has one, follows the "Invalid permanent" text.
     */
    public String rejectedChoiceMessage(GameData gameData, PermanentChoiceContext context, UUID chosenId) {
        if (context instanceof PermanentChoiceContext.AttackTriggerTarget att && gameData.playerIds.contains(chosenId)) {
            String reason = targetLegalityService.playerUntargetableReason(
                    gameData, chosenId, att.controllerId(), att.sourceCard());
            if (reason != null) {
                return reason;
            }
        }
        TargetFilter declaredFilter = context == null ? null : switch (context) {
            case PermanentChoiceContext.AttackTriggerTarget att -> triggerCollectionService.targetFilterOf(att);
            case PermanentChoiceContext.EntersTriggerTarget enters -> enters.targetFilter();
            case PermanentChoiceContext.ETBTokenMultiTargetTrigger multi ->
                    multi.currentGroupIndex() < multi.sourceCard().getTargetGroups().size()
                            ? multi.sourceCard().getTargetGroups().get(multi.currentGroupIndex()).getFilter()
                            : null;
            default -> null;
        };
        String restriction = null;
        if (declaredFilter instanceof com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter playerFilter
                && gameData.playerIds.contains(chosenId)) {
            // A player the trigger's own target restriction excludes (e.g. "target opponent").
            restriction = playerFilter.errorMessage();
        } else if (declaredFilter instanceof com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter permanentFilter
                && !gameData.playerIds.contains(chosenId)) {
            // A permanent the trigger's own target restriction excludes (e.g. "another nonland permanent").
            restriction = permanentFilter.errorMessage();
        }
        String message = "Invalid permanent: " + chosenId;
        return restriction == null ? message : message + ". " + restriction;
    }

    public void handleCopySpellForOtherControlledCreature(GameData gameData, UUID permanentId,
                                                          PermanentChoiceContext.CopySpellForOtherControlledCreatureChoice context) {
        copySpellHandler.completeChoice(gameData, permanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCopySpellForAnotherOpponentPermanent(GameData gameData, UUID permanentId,
                                                            PermanentChoiceContext.CopySpellForAnotherOpponentPermanentChoice context) {
        copySpellForAnotherOpponentPermanentHandler.completeChoice(gameData, permanentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDemonstrateOpponentChoice(GameData gameData, UUID opponentId,
                                                 PermanentChoiceContext.DemonstrateOpponentChoice context) {
        demonstrateEffectHandler.completeOpponentChoice(gameData, opponentId, context);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Completes {@code context}: {@code opponentId}, one of the ability controller's opponents, chooses its target,
     * which is prompted next.
     */
    public void handleChooseOpponentToChooseTriggerTarget(GameData gameData, UUID opponentId,
            PermanentChoiceContext.ChooseOpponentToChooseTriggerTarget context) {
        PermanentChoiceContext.AttackTriggerTarget target = context.target();
        if (opponentId == null || opponentId.equals(target.controllerId())
                || !gameData.orderedPlayerIds.contains(opponentId)) {
            return;
        }
        gameData.queueInteractionFirst(target.withChoosingPlayer(opponentId));
        triggerCollectionService.processNextAttackTriggerTarget(gameData);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCopyPermanentTargetedBySpell(GameData gameData, UUID permanentId) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("No pending effect resolution for permanent choice");
        }

        pendingEntry.setTargetId(permanentId);
        gameData.rerunCurrentEffectAfterInteraction = false;
        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry, gameData.pendingEffectResolutionIndex);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private boolean hasOptionalSingleTarget(Card card, List<CardEffect> effects) {
        if (effects.stream().anyMatch(OptionalTargetEffect.class::isInstance)) {
            return true;
        }
        return card.getTargetGroups().stream()
                .filter(target -> effects.stream().anyMatch(effect -> isBoundToTargetGroup(card, effect, target.getIndex())))
                .anyMatch(target -> target.getMinTargets() == 0 && target.getMaxTargets() == 1);
    }

    /** Whether the effect belongs to the card's target group. */
    private static boolean isBoundToTargetGroup(Card card, CardEffect effect, int targetIndex) {
        return card.getEffectTargetIndex(effect) == targetIndex;
    }

    /**
     * Decimator Beetle attack trigger, stage 1 response: the controller chose the creature they control
     * to remove a counter from. Now prompt for the optional second target (a creature the defending
     * player controls); if there is no legal second target, push the ability with just the first.
     */
    public void handleAttackCounterMoveFirstTarget(GameData gameData, UUID chosenId,
                                                   PermanentChoiceContext.AttackCounterMoveFirstTarget ctx) {
        List<UUID> validSecondTargets = ctx.defendingPlayerId() == null
                ? List.of()
                : triggerCollectionService.targetableCreaturesControlledBy(
                        gameData, ctx.defendingPlayerId(), ctx.sourceCard(), ctx.controllerId());

        if (validSecondTargets.isEmpty()) {
            pushAttackCounterMoveTrigger(gameData, ctx.sourceCard(), ctx.triggerRecord(), chosenId, null,
                    ctx.defendingPlayerId());
            continueAfterAttackCounterMove(gameData);
            return;
        }

        PermanentChoiceContext.AttackCounterMoveSecondTarget second =
                new PermanentChoiceContext.AttackCounterMoveSecondTarget(ctx.sourceCard(), ctx.controllerId(),
                        ctx.effects(), ctx.sourcePermanentId(), ctx.defendingPlayerId(), chosenId,
                        ctx.triggerRecord());
        gameData.interaction.setPermanentChoiceContext(second);
        playerInputService.beginAnyTargetChoice(gameData, ctx.controllerId(), validSecondTargets,
                List.of(ctx.controllerId()), ctx.sourceCard().getName()
                        + "'s ability - Choose up to one target creature that player controls (choose yourself to decline).");
        gameLogService.append(gameData, GameLog.cardThen(ctx.sourceCard(),
                "'s attack trigger - choose up to one target creature the defending player controls."));
    }

    /**
     * Decimator Beetle attack trigger, stage 2 response: the controller chose the (optional) creature the
     * defending player controls, or chose themselves to decline. Push the ability with both targets.
     */
    public void handleAttackCounterMoveSecondTarget(GameData gameData, UUID chosenId,
                                                    PermanentChoiceContext.AttackCounterMoveSecondTarget ctx) {
        UUID secondTargetId = chosenId.equals(ctx.controllerId()) ? null : chosenId;
        pushAttackCounterMoveTrigger(gameData, ctx.sourceCard(), ctx.triggerRecord(), ctx.firstTargetId(),
                secondTargetId, ctx.defendingPlayerId());
        continueAfterAttackCounterMove(gameData);
    }

    /** Finishes the ability the stack door parked for its two targets, the second "up to one" (CR 603.3d). */
    private void pushAttackCounterMoveTrigger(GameData gameData, Card sourceCard, StackEntry record,
                                              UUID firstTargetId, UUID secondTargetId, UUID defendingPlayerId) {
        List<UUID> targetIds = new ArrayList<>();
        targetIds.add(firstTargetId);
        if (secondTargetId != null) {
            targetIds.add(secondTargetId);
        }
        triggeredAbilityStackService.finishParked(gameData, record, entry -> {
            entry.setNonTargeting(false);
            entry.setTargetId(null);
            entry.chooseTargetIds(targetIds);
            entry.setRequiredTargetControllerId(defendingPlayerId);
        });

        String logEntry = sourceCard.getName() + "'s ability targets " + getTargetDisplayName(gameData, firstTargetId);
        if (secondTargetId != null) {
            logEntry += " and " + getTargetDisplayName(gameData, secondTargetId);
        }
        gameLogService.append(gameData, GameLog.text(logEntry + "."));
        log.info("Game {} - {} attack counter-move trigger pushed onto stack", gameData.id, sourceCard.getName());
    }

    private void continueAfterAttackCounterMove(GameData gameData) {
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRandomOpponentDamageChoice(GameData gameData, UUID permanentId,
                                                  PermanentChoiceContext.RandomOpponentDamageChoice choice) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            return;
        }

        pendingEntry.setTargetId(permanentId);
        gameLogService.append(gameData, GameLog.text(
                choice.sourceCard().getName() + " chooses " + getTargetDisplayName(gameData, permanentId) + "."));
        effectResolutionService.resolveEffectsFrom(
                gameData, pendingEntry, gameData.pendingEffectResolutionIndex);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    public void handleResolvingModalTarget(GameData gameData, UUID permanentId,
                                            PermanentChoiceContext.ResolvingModalTarget context) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Modal target choice has no parked resolution");
        }

        pendingEntry.setTargetId(permanentId);
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (gameData.playerIds.contains(permanentId)) {
            gameLogService.append(gameData, GameLog.builder().card(context.sourceCard())
                    .text("'s ability targets " + gameData.playerIdToName.get(permanentId) + ".").build());
        } else if (target != null) {
            gameLogService.append(gameData, GameLog.cardTextCard(context.sourceCard(), "'s ability targets ",
                    target.getCard(), "."));
        }

        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry, gameData.pendingEffectResolutionIndex);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAttackTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.AttackTriggerTarget att) {
        finishParkedTarget(gameData, permanentId, att);
        continueAfterAttackTrigger(gameData);
    }

    /**
     * Finishes an ability the stack door parked for its target ({@code TriggeredAbilityStackService.finishParked}): the
     * chosen permanent, player, or graveyard or exiled card, or no target when its controller declined an optional one.
     * A target that no longer exists leaves the ability with no legal target, and it is removed (CR 603.3d).
     */
    private void finishParkedTarget(GameData gameData, UUID chosenId, PermanentChoiceContext.AttackTriggerTarget att) {
        Permanent target = gameQueryService.findPermanentById(gameData, chosenId);
        boolean exiledCard = target == null && gameQueryService.findCardInExileById(gameData, chosenId) != null;
        boolean player = target == null && gameData.playerIdToName.containsKey(chosenId);
        boolean graveyardCard = target == null && !exiledCard && !player
                && gameQueryService.findCardInGraveyardById(gameData, chosenId) != null;
        boolean spell = target == null && !exiledCard && !player && !graveyardCard
                && gameData.stack.stream().anyMatch(stacked -> chosenId.equals(stacked.getTargetableId()));
        boolean declined = player && chosenId.equals(att.controllerId())
                && triggerCollectionService.isOptionalSingleTarget(att.sourceCard(), att.effects());
        if (declined) {
            triggeredAbilityStackService.finishParked(gameData, att.triggerRecord(), entry -> {
                entry.replaceEffectsToResolve(att.effects());
                entry.setTargetId(null);
                entry.setNonTargeting(false);
            });
            gameLogService.append(gameData, GameLog.cardThen(att.sourceCard(), "'s ability targets nothing."));
            return;
        }
        if (target == null && !player && !exiledCard && !graveyardCard && !spell) {
            gameLogService.append(gameData, GameLog.cardThen(att.sourceCard(), "'s ability has no valid target."));
            return;
        }
        TargetFilter targetFilter = exiledCard || graveyardCard || spell ? null
                : triggerCollectionService.targetFilterOf(att);
        triggeredAbilityStackService.finishParked(gameData, att.triggerRecord(), entry -> {
            entry.replaceEffectsToResolve(att.effects());
            entry.setTargetId(chosenId);
            entry.setNonTargeting(false);
            entry.setTargetZone(null);
            if (exiledCard || graveyardCard) {
                entry.setTargetZone(graveyardCard ? Zone.GRAVEYARD : Zone.EXILE);
            } else if (spell) {
                entry.setTargetZone(Zone.STACK);
            } else if (targetFilter != null) {
                entry.setTargetFilter(targetFilter);
            }
        });
        gameLogService.append(gameData, GameLog.builder().card(att.sourceCard())
                .text("'s ability targets " + getTargetDisplayName(gameData, chosenId) + ".").build());
    }

    /** What follows an attack trigger's target choice: the next queued target choice, then the may abilities. */
    private void continueAfterAttackTrigger(GameData gameData) {
        if (gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            triggerCollectionService.processNextAttackTriggerTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            triggerCollectionService.processNextETBTokenMultiTargetTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleAttackTrigger(GameData gameData, List<UUID> targetIds,
                                     MultiPermanentChoiceContext.AttackTriggerTargets context) {
        PermanentChoiceContext.AttackTriggerTarget att = context.pending();
        triggeredAbilityStackService.finishParked(gameData, att.triggerRecord(), entry -> {
            entry.replaceEffectsToResolve(att.effects());
            entry.setTargetId(null);
            entry.setNonTargeting(false);
            entry.chooseTargetIds(targetIds);
        });
        gameLogService.append(gameData, GameLog.builder().card(att.sourceCard()).text("'s ability targets "
                + targetIds.size() + " permanent" + (targetIds.size() == 1 ? "" : "s") + ".").build());
        continueAfterAttackTrigger(gameData);
    }

    public void handlePreparedOpponentTokenCopiesAttacking(GameData gameData, UUID attackTargetId,
            PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking context) {
        tokenCopySupport.completeOpponentAttackTargetChoice(gameData, attackTargetId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleTokenAttackTargetChoice(GameData gameData, UUID attackTargetId,
            PermanentChoiceContext.TokenAttackTargetChoice context) {
        Permanent token = gameQueryService.findPermanentById(gameData, context.tokenIds().getFirst());
        if (token != null) {
            token.setAttackTarget(attackTargetId);
        }
        List<UUID> remaining = context.tokenIds().subList(1, context.tokenIds().size());
        if (createdTokenAttackTargetsHandler.chooseAttackTargets(gameData, context.controllerId(), remaining)) {
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreparedTokenCopyAttachment(GameData gameData, UUID attachmentId,
            PermanentChoiceContext.PreparedTokenCopyAttachments context) {
        tokenCopySupport.completeTokenCopyAttachmentChoice(gameData, attachmentId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRevealUntilCardPredicateAttackTarget(
            GameData gameData, UUID attackTargetId,
            PermanentChoiceContext.RevealUntilCardPredicateAttackTarget context) {
        revealUntilCardHandler.completeAttackTargetChoice(gameData, attackTargetId, context);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleChosenPermanentAttackTarget(GameData gameData, UUID attackTargetId,
                                                   PermanentChoiceContext.ChosenPermanentAttackTarget context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, context.permanentId());
        boolean legalForRestrictedChoice = context.requiredAttackingPlayerId() == null
                || currentAttackTargetIds(gameData, context.requiredAttackingPlayerId()).contains(attackTargetId);
        if (permanent != null && gameQueryService.isCreature(gameData, permanent) && legalForRestrictedChoice) {
            permanent.enterAttacking(true);
            permanent.setAttackedOrBlockedSinceLastUpkeep(true);
            permanent.setAttackTarget(attackTargetId);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private java.util.Set<UUID> currentAttackTargetIds(GameData gameData, UUID attackingPlayerId) {
        java.util.Set<UUID> attackTargetIds = new java.util.LinkedHashSet<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(attackingPlayerId, List.of())) {
            if (permanent.isAttacking() && permanent.getAttackTarget() != null) {
                attackTargetIds.add(permanent.getAttackTarget());
            }
        }
        return attackTargetIds;
    }

    public void handleReselectAttackingCreatureTarget(GameData gameData, UUID attackTargetId,
                                                       PermanentChoiceContext.ReselectAttackingCreatureTarget context) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, context.permanentId());
        UUID attackerControllerId = attacker == null
                ? null : gameQueryService.findPermanentController(gameData, attacker.getId());
        boolean validTarget = attackerControllerId != null
                && attackTargetId != null
                && !attackerControllerId.equals(attackTargetId)
                && attackLegalityService.getValidReselectionTargetIds(gameData, attackerControllerId)
                .contains(attackTargetId);
        if (attacker != null && attacker.isAttacking()
                && gameQueryService.isCreature(gameData, attacker) && validTarget) {
            attacker.setAttackTarget(attackTargetId);
            attacker.setAttackTargetReselected(true);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleReselectedAttackTarget(GameData gameData, UUID attackTargetId,
                                             PermanentChoiceContext.ReselectAttackTarget context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, context.permanentId());
        if (permanent != null && permanent.isAttacking() && gameQueryService.isCreature(gameData, permanent)) {
            permanent.setAttackTarget(attackTargetId);
            permanent.setAttackTargetReselected(true);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleExileReturnAttackTarget(GameData gameData, UUID attackTargetId,
                                               PermanentChoiceContext.ExileReturnAttackTarget context) {
        turnProgressionService.completePendingExileReturnAttackTarget(gameData, attackTargetId, context);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleEntersTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.EntersTriggerTarget ett) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isExiledCardTarget = target == null
                && gameQueryService.findCardInExileById(gameData, permanentId) != null;
        boolean isPlayerTarget = target == null && gameData.playerIdToName.containsKey(permanentId);
        boolean declined = hasOptionalSingleTarget(ett.sourceCard(), ett.effects())
                && isPlayerTarget
                && permanentId.equals(ett.controllerId());
        if (target != null || isPlayerTarget || isExiledCardTarget) {
            // The chosen mode's target: the ability the stack door parked for its mode goes on the stack with it.
            triggeredAbilityStackService.finishParked(gameData, ett.triggerRecord(), ett.sourceCard(), entry -> {
                entry.replaceEffectsToResolve(ett.effects());
                entry.setNonTargeting(false);
                entry.setTargetId(declined ? null : permanentId);
                entry.setTargetZone(isExiledCardTarget ? Zone.EXILE : null);
            });
            if (declined) {
                gameLogService.append(gameData, GameLog.cardThen(ett.sourceCard(), "'s ability targets nothing."));
            } else {
                String targetName = getTargetDisplayName(gameData, permanentId);
                gameLogService.append(gameData, GameLog.builder().card(ett.sourceCard())
                        .text("'s ability targets " + targetName + ".").build());
            }
        } else {
            gameLogService.append(gameData, GameLog.cardThen(ett.sourceCard(), "'s ability has no valid target."));
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)) {
            triggerCollectionService.processNextEntersTriggerTarget(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePlayerWithLowestLifeChoice(GameData gameData, UUID playerId,
                                                  PermanentChoiceContext.PlayerWithLowestLifeChoice context) {
        Permanent source = context.sourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, context.sourcePermanentId())
                : findPermanentByCardId(gameData, context.sourceCard().getId());
        if (source != null) {
            creatureControlService.applyControlEffect(gameData, playerId, source,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    EffectDuration.PERMANENT, null, context.sourceCard().getName());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleBlackGateMostLifeChoice(GameData gameData, UUID playerId,
                                               PermanentChoiceContext.BlackGateMostLifeChoice context) {
        blackGateHandler.completeChoice(gameData, playerId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleTheMasterMostLifeChoice(GameData gameData, UUID playerId,
                                               PermanentChoiceContext.TheMasterMostLifeChoice context) {
        theMasterHandler.completeMostLifeChoice(gameData, playerId, context);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleLeastToughnessDamageChoice(GameData gameData, UUID permanentId,
                                                  PermanentChoiceContext.LeastToughnessDamageChoice context) {
        leastToughnessDamageSupport.handleChoice(gameData, permanentId, context);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }

    public void handlePucasMischiefOwnTarget(GameData gameData, UUID permanentId,
                                             PermanentChoiceContext.PucasMischiefOwnTarget pmot) {
        // Step 1 complete: nonland permanent you control chosen. Collect opponent nonland permanents
        // with mana value <= the chosen permanent's for step 2.
        Permanent ownTarget = gameQueryService.findPermanentById(gameData, permanentId);
        int ownManaValue = ownTarget != null ? ownTarget.getCard().getManaValue() : 0;
        ExchangeControlOfTargetPermanentsEffect exchange = pmot.effects().stream()
                .map(effect -> effect instanceof com.github.laxika.magicalvibes.model.effect.MayEffect may
                        ? may.wrapped() : effect)
                .filter(ExchangeControlOfTargetPermanentsEffect.class::isInstance)
                .map(ExchangeControlOfTargetPermanentsEffect.class::cast)
                .findFirst().orElse(null);

        List<UUID> validOpponentTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(pmot.controllerId())) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if ((exchange != null && exchange.requireOpponentPowerNotGreater()
                        ? ownTarget != null && gameQueryService.isCreature(gameData, p)
                                && gameQueryService.getEffectivePower(gameData, p)
                                <= gameQueryService.getEffectivePower(gameData, ownTarget)
                        : !p.getCard().hasType(CardType.LAND)
                                && p.getCard().getManaValue() <= ownManaValue)
                        && !validTargetService.isBlockedByHexproofOrGrantedEffect(
                                gameData, p, pmot.controllerId())) {
                    validOpponentTargets.add(p.getId());
                }
            }
        }

        if (validOpponentTargets.isEmpty()) {
            // No legal opponent permanent — the trigger does nothing (should not happen after
            // the step-1 pre-filter, but guarded for safety).
            log.info("Game {} - {} has no legal opponent target", gameData.id, pmot.sourceCard().getName());
            continueAfterPucasMischief(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.PucasMischiefOpponentTarget(
                pmot.sourceCard(), pmot.controllerId(), pmot.effects(), pmot.sourcePermanentId(), permanentId,
                pmot.triggerRecord()));
        playerInputService.beginPermanentChoice(gameData, pmot.controllerId(), validOpponentTargets,
                pmot.sourceCard().getName()
                        + " — Choose a nonland permanent an opponent controls with equal or lesser mana value.");

        log.info("Game {} - {} upkeep trigger awaiting opponent target selection",
                gameData.id, pmot.sourceCard().getName());
    }

    public void handlePucasMischiefOpponentTarget(GameData gameData, UUID permanentId,
                                                  PermanentChoiceContext.PucasMischiefOpponentTarget pmot) {
        // Step 2 complete: build the stack entry with both targets. The MayEffect wrapper carried in
        // effects() prompts the "you may" at resolution (CR 603.5), then the exchange resolves.
        List<UUID> targetIds = new ArrayList<>(List.of(pmot.ownTargetId(), permanentId));
        triggeredAbilityStackService.finishParked(gameData, pmot.triggerRecord(), entry -> {
            entry.setNonTargeting(false);
            entry.setTargetId(null);
            entry.chooseTargetIds(targetIds);
        });

        Permanent ownTarget = gameQueryService.findPermanentById(gameData, pmot.ownTargetId());
        Permanent opponentTarget = gameQueryService.findPermanentById(gameData, permanentId);
        String ownName = ownTarget != null ? ownTarget.getCard().getName() : pmot.ownTargetId().toString();
        String oppName = opponentTarget != null ? opponentTarget.getCard().getName() : permanentId.toString();
        
        gameLogService.append(gameData, GameLog.builder().card(pmot.sourceCard()).text("'s ability targets " + ownName + " and " + oppName + ".").build());
        log.info("Game {} - {} upkeep trigger targets {} and {}",
                gameData.id, pmot.sourceCard().getName(), ownName, oppName);

        continueAfterPucasMischief(gameData);
    }

    void continueAfterPucasMischief(GameData gameData) {
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

    public void handleETBSpellTargetTrigger(GameData gameData, UUID cardId, PermanentChoiceContext.ETBSpellTargetTrigger etbStt) {
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, cardId);
        String targetName = targetPermanent == null ? "" : targetPermanent.getCard().getName();
        Zone targetZone = targetPermanent == null ? Zone.STACK : null;
        if (targetPermanent == null) {
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(cardId)) {
                    targetName = se.getCard().getName();
                    break;
                }
            }
        }

        // The chosen mode's spell target: the ability the stack door parked for its mode goes on the stack with it.
        triggeredAbilityStackService.finishParked(gameData, etbStt.triggerRecord(), etbStt.sourceCard(), entry -> {
            entry.replaceEffectsToResolve(etbStt.effects());
            entry.setNonTargeting(false);
            entry.setTargetId(cardId);
            entry.setTargetZone(targetZone);
        });
        gameLogService.append(gameData, GameLog.builder().card(etbStt.sourceCard()).text("'s ETB ability targets " + targetName + ".").build());
        log.info("Game {} - {} ETB spell-target trigger targets {}", gameData.id, etbStt.sourceCard().getName(), targetName);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)) {
            etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleETBTokenMultiTargetTrigger(GameData gameData, UUID chosenId,
                                                   PermanentChoiceContext.ETBTokenMultiTargetTrigger etbMtt) {
        int chosenInGroup = etbMtt.chosenInCurrentGroup();
        int groupMin = etbTokenTargetService.effectiveMinTargets(gameData, etbMtt);

        // Once the group's minimum has been met, a response equal to the controller's own
        // player ID means "stop adding targets in this group" — unless the controller is itself a
        // legal target for the group, in which case it targets them.
        boolean groupDone = chosenInGroup >= groupMin
                && chosenId.equals(etbMtt.controllerId())
                && etbTokenTargetService.controllerChoiceStopsGroup(gameData, etbMtt);
        advanceETBTokenMultiTargetTrigger(gameData, etbMtt, groupDone ? null : chosenId);
    }

    /**
     * Adds {@code chosenId} to the current target group of a multi-target ETB trigger, or ends the
     * group when it is {@code null}, then prompts for the next slot or puts the ability on the stack.
     */
    private void advanceETBTokenMultiTargetTrigger(GameData gameData,
                                                   PermanentChoiceContext.ETBTokenMultiTargetTrigger etbMtt,
                                                   UUID chosenId) {
        int groupIdx = etbMtt.currentGroupIndex();
        int chosenInGroup = etbMtt.chosenInCurrentGroup();
        boolean groupDone = chosenId == null;

        List<UUID> updatedChosen;
        int nextGroupIdx;
        int nextChosenInGroup;
        List<Integer> updatedGroupSizes = etbMtt.groupSizes();
        if (groupDone) {
            updatedChosen = etbMtt.chosenTargetsSoFar();
            nextGroupIdx = groupIdx + 1;
            nextChosenInGroup = 0;
            updatedGroupSizes = new ArrayList<>(updatedGroupSizes);
            updatedGroupSizes.add(chosenInGroup);
            log.info("Game {} - {} ETB multi-target trigger ended group {} after {} target(s)",
                    gameData.id, etbMtt.sourceCard().getName(), groupIdx, chosenInGroup);
        } else {
            updatedChosen = new ArrayList<>(etbMtt.chosenTargetsSoFar());
            updatedChosen.add(chosenId);
            nextGroupIdx = groupIdx;
            nextChosenInGroup = chosenInGroup + 1;
            String targetName = getTargetDisplayName(gameData, chosenId);
            
            gameLogService.append(gameData, GameLog.builder().card(etbMtt.sourceCard()).text("'s ETB ability — target " + (groupIdx + 1) + ": " + targetName + ".").build());
            log.info("Game {} - {} ETB multi-target trigger chose {} (group {} slot {})",
                    gameData.id, etbMtt.sourceCard().getName(), targetName, groupIdx, chosenInGroup);
        }

        // Replace head of the queue with the advanced state; then let the processor decide
        // whether to prompt again (more slots / next group) or push the final ETB.
        gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        gameData.queueInteractionFirst(etbMtt.withProgress(updatedChosen, nextGroupIdx, nextChosenInGroup,
                List.copyOf(updatedGroupSizes)));

        etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        if (etbMtt.resumePendingMayResolution()) {
            gameData.resolvedMayAccepted = true;
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(
                        gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)) {
            etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleEtbPlayerTargetGroup(GameData gameData, List<UUID> targets,
            MultiPermanentChoiceContext.EtbPlayerTargetGroup context) {
        var pending = context.pending();
        List<UUID> chosen = new ArrayList<>(pending.chosenTargetsSoFar());
        chosen.addAll(targets);
        var completedGroup = pending.withProgress(chosen, pending.currentGroupIndex(),
                pending.chosenInCurrentGroup() + targets.size(), pending.groupSizes());
        advanceETBTokenMultiTargetTrigger(gameData, completedGroup, null);
    }

    public void handleEtbGraveyardCardTargetGroup(GameData gameData, List<UUID> targets,
            MultiPermanentChoiceContext.EtbGraveyardCardTargetGroup context) {
        var pending = context.pending();
        if (targets.isEmpty()) {
            advanceETBTokenMultiTargetTrigger(gameData, pending, null);
            return;
        }
        List<UUID> chosen = new ArrayList<>(pending.chosenTargetsSoFar());
        chosen.addAll(targets);
        var completedGroup = pending.withProgress(chosen, pending.currentGroupIndex(),
                pending.chosenInCurrentGroup() + targets.size(), pending.groupSizes());
        advanceETBTokenMultiTargetTrigger(gameData, completedGroup, null);
    }

    public void handleDayNightTransformAttachment(GameData gameData, UUID chosenId,
            PermanentChoiceContext.DayNightTransformAttachment dnta) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, dnta.permanentId());
        if (permanent != null && permanent.getCard().isAura() && permanent.getCard().isEnchantPlayer()) {
            permanent.setAttachedTo(chosenId);
            permanent.setTimestamp(gameData.nextTimestamp());
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                    " attaches to " + getTargetDisplayName(gameData, chosenId) + "."));
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DayNightTransformAttachment.class)) {
            triggerCollectionService.processNextDayNightTriggerTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSagaChapterTarget(GameData gameData, UUID chosenId, PermanentChoiceContext.SagaChapterTarget sct) {
        if (!sct.targetGroups().isEmpty()) {
            handleSagaChapterMultiTarget(gameData, chosenId, sct);
            return;
        }

        // If the player chose their own player ID, it means "skip" (up to one — chose zero)
        boolean skipped = gameData.playerIdToName.containsKey(chosenId)
                && chosenId.equals(sct.controllerId());

        Permanent target = skipped ? null : gameQueryService.findPermanentById(gameData, chosenId);
        if (skipped || target == null) {
            // "Up to one" declined, or the target no longer exists: the ability goes on the stack with no target.
            triggeredAbilityStackService.finishParked(gameData, sct.triggerRecord(), entry -> entry.setNonTargeting(false));
            gameLogService.append(gameData, GameLog.builder().card(sct.sourceCard()).text("'s chapter "
                    + sct.chapterName() + (skipped ? " targets no creature." : " target no longer exists.")).build());
        } else {
            TargetFilter chapterFilter = sct.targetFilters().isEmpty() ? null : sct.targetFilters().iterator().next();
            triggeredAbilityStackService.finishParked(gameData, sct.triggerRecord(), entry -> {
                entry.setTargetId(chosenId);
                entry.setNonTargeting(false);
                if (chapterFilter != null) {
                    entry.setTargetFilter(chapterFilter);
                }
            });
            String targetName = getTargetDisplayName(gameData, chosenId);
            gameLogService.append(gameData, GameLog.builder().card(sct.sourceCard()).text("'s chapter "
                    + sct.chapterName() + " targets " + targetName + ".").build());
            log.info("Game {} - {} chapter {} targets {}", gameData.id, sct.sourceCard().getName(), sct.chapterName(),
                    targetName);
        }
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSagaChapterPlayerTarget(GameData gameData, UUID chosenId,
                                               PermanentChoiceContext.SagaChapterPlayerTarget scpt) {
        triggeredAbilityStackService.finishParked(gameData, scpt.triggerRecord(), entry -> {
            entry.setTargetId(chosenId);
            entry.setNonTargeting(false);
        });

        gameLogService.append(gameData, GameLog.builder().card(scpt.sourceCard())
                .text("'s chapter " + scpt.chapterName() + " targets "
                        + gameData.playerIdToName.get(chosenId) + ".").build());
        log.info("Game {} - {} chapter {} targets player {}", gameData.id,
                scpt.sourceCard().getName(), scpt.chapterName(), gameData.playerIdToName.get(chosenId));

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class)) {
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
            return;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleSagaChapterMultiTarget(GameData gameData, UUID chosenId,
                                               PermanentChoiceContext.SagaChapterTarget sct) {
        SagaChapterTargetGroup group = sct.targetGroups().get(sct.currentGroupIndex());
        boolean skipped = group.minTargets() == 0
                && gameData.playerIdToName.containsKey(chosenId)
                && chosenId.equals(sct.controllerId());
        List<UUID> chosenTargets = new ArrayList<>(sct.chosenTargetsSoFar());
        if (!skipped) {
            chosenTargets.add(chosenId);
        }

        boolean repeatVariableLastGroup = !skipped
                && sct.targetGroups().size() == 1
                && chosenTargets.size() < group.maxTargets();

        gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
        gameData.queueInteractionFirst(sct.withProgress(chosenTargets,
                repeatVariableLastGroup ? sct.currentGroupIndex() : sct.currentGroupIndex() + 1,
                repeatVariableLastGroup
                        ? sct.completedGroupTargetCounts()
                        : sct.closeCurrentGroup(chosenTargets.size())));
        triggerCollectionService.processNextSagaChapterTarget(gameData);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private String getTargetDisplayName(GameData gameData, UUID targetId) {
        String playerName = gameData.playerIdToName.get(targetId);
        if (playerName != null) return playerName;

        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(targetId)) return se.getDescription();
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(targetId)) return p.getCard().getName();
            }
        }

        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        if (graveyardCard != null) return graveyardCard.getName();

        Card exiledCard = gameQueryService.findCardInExileById(gameData, targetId);
        if (exiledCard != null) return exiledCard.getName();

        return targetId.toString();
    }

}
