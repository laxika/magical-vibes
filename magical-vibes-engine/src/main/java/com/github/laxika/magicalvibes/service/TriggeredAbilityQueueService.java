package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.trigger.CastAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.TargetGroup;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OptionalTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.IndependentlyTargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggeredAbilityQueueService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerTargetCollector triggerTargetCollector;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private ReturnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler
            returnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.SagaChapterCounterDistributionSupport
            sagaChapterCounterDistributionSupport;
    private final com.github.laxika.magicalvibes.service.target.TargetLegalityService targetLegalityService;
    private final com.github.laxika.magicalvibes.service.target.ValidTargetService validTargetService;
    private final AmountEvaluationService amountEvaluationService;
    @Lazy private final ETBTokenTargetService etbTokenTargetService;
    @Lazy private final GraveyardTargetingService graveyardTargetingService;

    private com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService
            triggeredAbilityStackService;

    @Autowired
    void setTriggeredAbilityStackService(
            @Lazy com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService service) {
        this.triggeredAbilityStackService = service;
    }

    @Autowired
    void setReturnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler(
            @Lazy ReturnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler handler) {
        this.returnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler = handler;
    }

    public void processNextDayNightTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DayNightTransformAttachment.class)) {
            PermanentChoiceContext.DayNightTransformAttachment pending =
                    gameData.pollPendingInteraction(PermanentChoiceContext.DayNightTransformAttachment.class);
            Permanent permanent = gameQueryService.findPermanentById(gameData, pending.permanentId());
            if (permanent == null || !permanent.getCard().isAura() || !permanent.getCard().isEnchantPlayer()) {
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPlayerChoice(gameData, pending.controllerId(), gameData.orderedPlayerIds,
                    permanent.getCard().getName() + " - Choose a player to enchant.");
            return;
        }
    }

    /**
     * Whether the one target of {@code effects}, an ability of {@code card}, is "up to one": an optional target of its
     * own, or the card's one group of up to one, or the group of the card bound to the effects, of up to one target.
     */
    public boolean isOptionalSingleTarget(Card card, List<CardEffect> effects) {
        if (hasOptionalSingleTarget(card, effects)) {
            return true;
        }
        TargetGroup group = targetGroupForTriggeredEffects(card, effects);
        return group != null && group.getMinTargets() == 0 && group.getMaxTargets() == 1
                && group.getDynamicMinTargets() == null && group.getDynamicMaxTargets() == null;
    }

    private boolean hasOptionalSingleTarget(Card card, List<CardEffect> effects) {
        if (effects.stream().anyMatch(effect -> effect instanceof OptionalTargetEffect
                || effect.hasOptionalTarget())) {
            return true;
        }
        if (card.getTargetGroups().size() != 1) {
            return false;
        }
        var target = card.getTargetGroups().getFirst();
        return target.getMinTargets() == 0
                && target.getMaxTargets() == 1
                && effects.stream().anyMatch(effect -> isBoundToTargetGroup(card, effect, target.getIndex()));
    }

    /** Whether the effect belongs to the card's target group. */
    private static boolean isBoundToTargetGroup(Card card, CardEffect effect, int targetIndex) {
        return card.getEffectTargetIndex(effect) == targetIndex;
    }

    private TargetGroup targetGroupForTriggeredEffects(Card sourceCard, List<CardEffect> effects) {
        int targetGroupIndex = effects.stream()
                .mapToInt(sourceCard::getEffectTargetIndex)
                .filter(index -> index >= 0 && index < sourceCard.getTargetGroups().size())
                .findFirst()
                .orElse(-1);
        if (targetGroupIndex >= 0) {
            return sourceCard.getTargetGroups().get(targetGroupIndex);
        }
        // A card's one bare group, bound to no effect, is the target of each of its abilities (Protector of the Wastes'
        // "up to two target artifacts and/or enchantments" as it enters and as it becomes monstrous); an Aura's first
        // group is what it enchants.
        List<TargetGroup> groups = sourceCard.getTargetGroups();
        return groups.size() == 1 && !sourceCard.isAura()
                && !sourceCard.bindsEffectToTargetGroup(groups.getFirst().getIndex()) ? groups.getFirst() : null;
    }

    /**
     * The target filter of {@code pending}'s ability: its own ({@link TriggeredAbility#targetFilter()}, which the stack
     * door stamps on the record: a granted ability's "target opponent"), else the one the card declares for its
     * effects.
     */
    public TargetFilter targetFilterOf(PermanentChoiceContext.AttackTriggerTarget pending) {
        if (pending.triggerRecord() != null && pending.triggerRecord().getTargetFilter() != null) {
            return pending.triggerRecord().getTargetFilter();
        }
        List<CardEffect> abilityEffects = pending.effects();
        if (pending.triggerRecord() != null && pending.triggerRecord().isMadeByResolution()) {
            // An ability a resolution made is no other ability's: only a group its own effects are bound to is its.
            Card card = pending.sourceCard();
            return abilityEffects.stream().mapToInt(card::getEffectTargetIndex)
                    .filter(index -> index >= 0 && index < card.getTargetGroups().size())
                    .mapToObj(index -> card.getTargetGroups().get(index).getFilter())
                    .findFirst().orElse(null);
        }
        return targetFilterForTriggeredEffects(pending.sourceCard(), abilityEffects);
    }

    public TargetFilter targetFilterForTriggeredEffects(Card sourceCard, List<CardEffect> effects) {
        int targetGroupIndex = effects.stream()
                .mapToInt(sourceCard::getEffectTargetIndex)
                .filter(index -> index >= 0 && index < sourceCard.getTargetGroups().size())
                .findFirst()
                .orElse(-1);
        if (targetGroupIndex < 0) {
            Set<Integer> compatibleTargetGroups = java.util.stream.Stream.concat(
                            java.util.stream.Stream.concat(sourceCard.getCastingCostsAndSpellEffects().stream(),
                                    sourceCard.getStaticEffects().stream()),
                            sourceCard.getTriggeredEffects().stream())
                    .filter(authored -> effects.stream()
                            .anyMatch(queued -> authored.getClass().equals(queued.getClass())))
                    .mapToInt(sourceCard::getEffectTargetIndex)
                    .filter(index -> index >= 0 && index < sourceCard.getTargetGroups().size())
                    .boxed()
                    .collect(Collectors.toSet());
            if (compatibleTargetGroups.size() == 1) {
                targetGroupIndex = compatibleTargetGroups.iterator().next();
            }
        }
        if (sourceCard.isAura() && targetGroupIndex < 0 && effects.stream()
                .anyMatch(effect -> !TargetSpec.NONE.equals(effect.targetSpec()))) {
            // An Aura's declared target is its enchantment target, not the target of an
            // unbound granted trigger. Let the trigger's own target specification decide
            // whether it can target players or permanents.
            return null;
        }
        if (targetGroupIndex < 0 && !sourceCard.getTargetGroups().isEmpty()
                && sourceCard.getTargetGroups().stream().allMatch(group ->
                sourceCard.bindsEffectToTargetGroup(group.getIndex())
                && sourceCard.getEffectsBoundToTargetGroup(group.getIndex()).stream()
                .allMatch(bound -> TargetSpec.NONE.equals(bound.targetSpec())
                        || effects.stream().noneMatch(queued -> queued.getClass().equals(bound.getClass()))))
                && effects.stream().anyMatch(effect -> !TargetSpec.NONE.equals(effect.targetSpec()))) {
            // Every declared group belongs to some other ability of the card (a temporary copy that kept
            // Hulkling's own cast trigger): the queued ability's own target specification decides.
            return null;
        }
        return targetGroupIndex >= 0
                ? sourceCard.getTargetGroups().get(targetGroupIndex).getFilter()
                : sourceCard.getTargetFilter();
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        while (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            PermanentChoiceContext.AttackTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
            if (pending.choosingPlayerId() == null) {
                // "Any target of an opponent's choice" (Karplusan Minotaur): the ability's controller chooses which of
                // their opponents chooses the target.
                gameData.pollPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
                List<UUID> opponents = gameData.orderedPlayerIds.stream()
                        .filter(playerId -> !playerId.equals(pending.controllerId()))
                        .toList();
                if (opponents.isEmpty()) {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s triggered ability has no opponent to choose its target."));
                    continue;
                }
                if (opponents.size() == 1) {
                    gameData.queueInteractionFirst(pending.withChoosingPlayer(opponents.getFirst()));
                    continue;
                }
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ChooseOpponentToChooseTriggerTarget(pending));
                playerInputService.beginPlayerChoice(gameData, pending.controllerId(), opponents,
                        pending.sourceCard().getName() + "'s ability - choose the opponent who chooses its target.");
                return;
            }
            UUID choosingPlayerId = pending.choosingPlayerId();
            Permanent sourcePermanent = pending.sourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());

            var combatBounce = pending.effects().stream()
                    .filter(com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect.class::isInstance)
                    .map(com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect.class::cast)
                    .filter(com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect::targetsChosenAtTriggerTime)
                    .findFirst().orElse(null);
            if (combatBounce != null) {
                List<UUID> validIds = gameData.playerBattlefields
                        .getOrDefault(pending.attackedTargetId(), List.of()).stream()
                        .filter(permanent -> combatBounce.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, combatBounce.filter()))
                        .filter(permanent -> validTargetService.isValidTriggeredAbilityPermanentTarget(gameData,
                                pending.sourceCard(), pending.effects(),
                                com.github.laxika.magicalvibes.model.filter.TargetFilters.permanent(),
                                permanent, pending.controllerId()))
                        .map(Permanent::getId).toList();
                gameData.pollPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
                // "Up to that many": the damage dealt, the choice's event value.
                int damage = combatBounce.fixedCount() > 0 ? combatBounce.fixedCount() : pending.eventValue();
                int maxCount = Math.min(damage, validIds.size());
                if (maxCount == 0) {
                    // "Up to that many" with none to choose: the ability goes on the stack with no target.
                    triggeredAbilityStackService.finishParked(gameData, pending.triggerRecord(), entry -> {
                        entry.replaceEffectsToResolve(pending.effects());
                        entry.setTargetId(null);
                        entry.setNonTargeting(false);
                    });
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            " has no permanents to return."));
                    continue;
                }
                playerInputService.beginMultiPermanentChoice(gameData, choosingPlayerId,
                        validIds, maxCount, new MultiPermanentChoiceContext.AttackTriggerTargets(pending, 0),
                        "Choose up to " + maxCount + " target permanents to return to their owners' hands.");
                return;
            }

            // The damage source of an optional attack trigger can be a different creature than the
            // ability's source (Rage Forger's "that creature deals 1 damage"). Target legality is judged
            // from the ability's source (CR 702.16b), so protection from the attacker's color must not
            // hide a planeswalker here; the damage itself is prevented later (CR 702.16e).
            Permanent targetingSource = sourcePermanent;
            Card targetingSourceCard = pending.sourceCard();
            if (sourcePermanent != null
                    && !sourcePermanent.getCard().getId().equals(pending.sourceCard().getId())) {
                Permanent abilityPermanent = gameQueryService.findPermanentByCardId(gameData, pending.sourceCard().getId());
                // An attack ability an Aura or Equipment grants to its creature has that creature as
                // its source (CR 113.7), so the attached permanent must not replace it.
                boolean grantedByAttachedPermanent = abilityPermanent != null
                        && abilityPermanent.isAttached()
                        && abilityPermanent.getAttachedTo().equals(sourcePermanent.getId());
                if (grantedByAttachedPermanent) {
                    targetingSourceCard = sourcePermanent.getCard();
                } else if (abilityPermanent != null) {
                    targetingSource = abilityPermanent;
                }
            }
            TargetFilter abilityTargetFilter = targetFilterOf(pending);
            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    abilityTargetFilter,
                    pending.controllerId(),
                    targetingSourceCard,
                    TriggerTargetCollector.Options.ATTACK,
                    targetingSource,
                    defendingPlayerId(gameData, pending.attackedTargetId()),
                    pending.xValue(), pending.triggeringPermanentId(), pending.triggeringPlayerId(), null,
                    pending.eventValue());
            boolean optionalTarget = isOptionalSingleTarget(pending.sourceCard(), pending.effects());
            TargetGroup targetGroup = targetGroupForTriggeredEffects(pending.sourceCard(), pending.effects());
            List<UUID> candidateTargets = new ArrayList<>(result.validTargets());
            // A player filter of the ability ("target opponent previously dealt damage by it") judges the players.
            if (abilityTargetFilter instanceof com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter
                    || abilityTargetFilter instanceof com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter) {
                List<UUID> legalPlayers = validTargetService.filterValidPlayerTargetsChosenBy(gameData,
                        abilityTargetFilter, candidateTargets.stream().filter(gameData.playerIds::contains).toList(),
                        choosingPlayerId, pending.controllerId(), pending.sourcePermanentId(),
                        pending.triggeringPlayerId());
                candidateTargets.removeIf(id -> gameData.playerIds.contains(id) && !legalPlayers.contains(id));
            }
            candidateTargets.addAll(result.validGraveyardCardIds());

            if (candidateTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
                boolean mayTargetNone = optionalTarget || targetGroup != null && targetGroup.getMinTargets() == 0
                        && targetGroup.getDynamicMinTargets() == null;
                if (mayTargetNone) {
                    // "Up to one target", "up to two target creatures" with none to choose: the ability goes on the
                    // stack with no target.
                    triggeredAbilityStackService.finishParked(gameData, pending.triggerRecord(), entry -> {
                        entry.replaceEffectsToResolve(pending.effects());
                        entry.setTargetId(null);
                        entry.setNonTargeting(false);
                    });
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s triggered ability triggers without a target."));
                } else {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s triggered ability has no valid targets."));
                    log.info("Game {} - {} attack trigger skipped (no valid targets)",
                            gameData.id, pending.sourceCard().getName());
                }
                continue;
            }

            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : result.canTargetExiledCards() ? "target face-up card in exile"
                            : !result.validGraveyardCardIds().isEmpty() && !result.canTargetPermanents()
                                    ? "target card in a graveyard"
                            : optionalTarget ? "target permanent or yourself to decline" : "target permanent";
            gameData.pollPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            if (targetGroup != null && targetGroup.getMaxTargets() > 1) {
                int minTargets = targetGroup.getMinTargets();
                int maxTargets = Math.min(targetGroup.getMaxTargets(), candidateTargets.size());
                if (pending.sourceCard().getMultiTargetConstraint() == MultiTargetConstraint.ONE_PER_PLAYER) {
                    boolean everyPlayerHasTarget = gameData.orderedPlayerIds.stream()
                            .allMatch(playerId -> candidateTargets.stream()
                                    .anyMatch(id -> playerId.equals(gameQueryService.findPermanentController(gameData, id))));
                    if (!everyPlayerHasTarget) {
                        gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                                "'s triggered ability has too few valid targets."));
                        continue;
                    }
                    minTargets = gameData.orderedPlayerIds.size();
                    maxTargets = minTargets;
                }
                if (candidateTargets.size() < minTargets) {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s triggered ability has too few valid targets."));
                    continue;
                }
                List<UUID> playerTargets = candidateTargets.stream()
                        .filter(gameData.playerIds::contains).toList();
                List<UUID> otherTargets = candidateTargets.stream()
                        .filter(id -> !gameData.playerIds.contains(id)).toList();
                String multiTargetPrompt = pending.sourceCard().getName() + "'s ability - Choose up to "
                        + maxTargets + " target permanents.";
                MultiPermanentChoiceContext multiTargetContext =
                        new MultiPermanentChoiceContext.AttackTriggerTargets(pending, minTargets);
                if (playerTargets.isEmpty()) {
                    playerInputService.beginMultiPermanentChoice(gameData, choosingPlayerId,
                            candidateTargets, maxTargets, multiTargetContext, multiTargetPrompt);
                } else {
                    playerInputService.beginMultiPermanentOrPlayerChoice(gameData, choosingPlayerId,
                            otherTargets, playerTargets, maxTargets, multiTargetContext, multiTargetPrompt);
                }
            } else if (optionalTarget) {
                playerInputService.beginAnyTargetChoice(gameData, choosingPlayerId, candidateTargets,
                        List.of(pending.controllerId()),
                        pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");
            } else if (candidateTargets.stream().anyMatch(gameData.playerIds::contains)) {
                playerInputService.beginAnyTargetChoice(gameData, choosingPlayerId,
                        candidateTargets.stream().filter(id -> !gameData.playerIds.contains(id)).toList(),
                        candidateTargets.stream().filter(gameData.playerIds::contains).toList(),
                        pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");
            } else {
                playerInputService.beginPermanentChoice(gameData, choosingPlayerId, candidateTargets,
                        pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");
            }

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability - choose " + targetDescription + "."));
            log.info("Game {} - {} attack trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        // CR 310.9d: for a battle being attacked, "defending player" refers to its protector.
        Permanent attacked = gameQueryService.findPermanentById(gameData, attackedTargetId);
        if (attacked != null && gameQueryService.isBattle(gameData, attacked)) {
            return attacked.getProtectorPlayerId();
        }
        return gameQueryService.findPermanentController(gameData, attackedTargetId);
    }

    /**
     * Stage 1 of Decimator Beetle's attack trigger: prompt the controller to choose the creature they
     * control that a counter is removed from. The stage-2 (defending-player) choice is begun by the
     * stage-1 response handler in {@code PermanentChoiceTriggerHandlerService}.
     */
    public void processNextAttackCounterMoveFirstTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class)) {
            PermanentChoiceContext.AttackCounterMoveFirstTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);

            List<UUID> validTargets = targetableCreaturesControlledBy(
                    gameData, pending.controllerId(), pending.sourceCard(), pending.controllerId());
            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s attack trigger has no valid targets."));
                log.info("Game {} - {} attack trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability - Choose target creature you control.");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s attack trigger - choose target creature you control."));
            log.info("Game {} - {} attack trigger awaiting first target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * Ids of every creature the given player controls that {@code sourceCard}, controlled by
     * {@code choosingPlayerId}, may legally target (excludes shroud / opponent-hexproof / protection).
     * Shared by both stages of the attack counter-move target choices.
     */
    public List<UUID> targetableCreaturesControlledBy(GameData gameData, UUID playerId,
                                                      Card sourceCard, UUID choosingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        List<UUID> result = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && targetLegalityService.checkTriggeredPermanentTargetableReason(
                            gameData, permanent, sourceCard, choosingPlayerId).isEmpty()) {
                result.add(permanent.getId());
            }
        }
        return result;
    }

    public void processNextEntersTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)) {
            PermanentChoiceContext.EntersTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);

            UUID choosingPlayerId = pending.choosingPlayerId() != null
                    ? pending.choosingPlayerId() : pending.controllerId();
            if (pending.enteringPermanentId() != null && pending.effects().stream().anyMatch(effect ->
                    effect instanceof MayEffect may && may.choicePlayer()
                            == com.github.laxika.magicalvibes.model.MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)) {
                UUID enteringControllerId = gameQueryService.findPermanentController(gameData, pending.enteringPermanentId());
                if (enteringControllerId != null) {
                    choosingPlayerId = enteringControllerId;
                }
            }

            TargetFilter targetFilter = pending.targetFilter() != null
                    ? pending.targetFilter()
                    : targetFilterForTriggeredEffects(pending.sourceCard(), pending.effects());
            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    targetFilter,
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.ATTACK,
                    pending.targetSourcePermanentId() == null
                            ? null
                            : gameQueryService.findPermanentById(gameData, pending.targetSourcePermanentId()),
                    null,
                    null,
                    pending.enteringPermanentId());
            boolean optionalTarget = hasOptionalSingleTarget(pending.sourceCard(), pending.effects());

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
                if (optionalTarget) {
                    // The chosen mode's "up to one" target with none to choose: the ability the stack door parked for
                    // its mode goes on the stack with the mode and no target.
                    triggeredAbilityStackService.finishParked(gameData, pending.triggerRecord(), pending.sourceCard(),
                            entry -> {
                                entry.replaceEffectsToResolve(pending.effects());
                                entry.setTargetId(null);
                                entry.setNonTargeting(false);
                            });
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s enter trigger triggers without a target."));
                    log.info("Game {} - {} enter trigger pushed without a target",
                            gameData.id, pending.sourceCard().getName());
                } else {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s enter trigger has no valid targets."));
                    log.info("Game {} - {} enter trigger skipped (no valid targets)",
                            gameData.id, pending.sourceCard().getName());
                }
                continue;
            }

            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : result.canTargetExiledCards() && result.canTargetPermanents()
                                    ? "target permanent or card in exile"
                                    : result.canTargetExiledCards()
                                            ? "target card in exile"
                                            : optionalTarget ? "target permanent or yourself to decline" : "target permanent";
            gameData.pollPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            List<UUID> validPlayerTargets = result.validTargets().stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            List<UUID> validPermanentTargets = result.validTargets().stream()
                    .filter(id -> !gameData.playerIds.contains(id))
                    .toList();
            if (optionalTarget || result.canTargetPlayers()) {
                List<UUID> playerChoices = optionalTarget && !validPlayerTargets.contains(pending.controllerId())
                        ? new ArrayList<>(List.of(pending.controllerId())) : validPlayerTargets;
                playerInputService.beginAnyTargetChoice(gameData, choosingPlayerId, validPermanentTargets,
                        playerChoices,
                        pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");
            } else {
                playerInputService.beginPermanentChoice(gameData, choosingPlayerId, validPermanentTargets,
                        pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");
            }

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s enter trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} enter trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextTriggeredModalTrigger(GameData gameData) {
        while (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            PermanentChoiceContext.TriggeredModalTrigger pending =
                    gameData.pollPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
            ChooseOneEffect effect = pending.effect();
            // How many modes are chosen where the trigger event set it ("choose up to X", Riku of Many Paths).
            if (pending.modeCount() != null) {
                effect = effect.withChoiceCount(pending.modeCount().minimum(), pending.modeCount().maximum());
            }
            // "That wasn't chosen during your last combat" (By Elspeth's Command): the mode the source chose last.
            boolean lastCombat = effect.modeRestriction() == ChooseOneEffect.ModeRestriction.NOT_CHOSEN_DURING_LAST_COMBAT;
            if (pending.consumeModes() || pending.modesResetEachTurn() || lastCombat) {
                Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
                Set<String> alreadyChosen = source == null ? Set.of()
                        : pending.consumeModes() ? source.getChosenModeLabels()
                        : pending.modesResetEachTurn() ? source.getChosenModeLabelsThisTurn()
                        : source.getChosenMode() == null ? Set.of() : Set.of(source.getChosenMode());
                List<ChooseOneEffect.ChooseOneOption> remaining = effect.options().stream()
                        .filter(option -> !alreadyChosen.contains(option.label()))
                        .toList();
                if (remaining.isEmpty()) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(pending.sourceCard(), "'s trigger has no modes left to choose."));
                    continue;
                }
                effect = new ChooseOneEffect(remaining);
            }
            List<ChooseOneEffect.ChooseOneOption> legalOptions = effect.options().stream()
                    .filter(option -> hasLegalTriggeredModeTarget(gameData, pending, option))
                    .toList();
            if (legalOptions.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s triggered ability has no legal modes."));
                continue;
            }
            if (legalOptions.size() != effect.options().size()) {
                effect = new ChooseOneEffect(legalOptions, effect.optional(), effect.choicesRequired(),
                        effect.choicesMax(), effect.allModesWhenOptionalCostPaid(), effect.modesMayRepeat(),
                        effect.additionalModesCondition());
            }
            playerInputService.beginTriggeredModalChoice(gameData, pending.controllerId(), pending.sourceCard(),
                    effect, pending.sourcePermanentId(), pending.modesResetEachTurn(), pending.consumeModes(),
                    List.of(), pending.triggeringCardId(), pending.attackedTargetId(),
                    pending.triggeringPermanentId(), pending.rememberLastChosenMode(), pending.triggerRecord());
            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(), "'s ability - choose a mode."));
            log.info("Game {} - {} triggered ability awaiting mode selection", gameData.id,
                    pending.sourceCard().getName());
            return;
        }
    }

    private boolean hasLegalTriggeredModeTarget(GameData gameData,
            PermanentChoiceContext.TriggeredModalTrigger pending,
            ChooseOneEffect.ChooseOneOption option) {
        GraveyardTargetingSupport.Target graveyardTarget = graveyardTargetingSupport.findTarget(option.effects());
        if (graveyardTarget != null && graveyardTarget.minTargets() > 0) {
            long validCards = graveyardTarget.scope().graveyardOwners(gameData.orderedPlayerIds, pending.controllerId())
                    .stream()
                    .mapToLong(ownerId -> gameData.playerGraveyards.getOrDefault(ownerId, List.of()).stream()
                            .filter(card -> graveyardTarget.filter() == null
                                    || predicateEvaluationService.matchesCardPredicate(card, graveyardTarget.filter(),
                                    pending.sourceCard().getId(), gameData, ownerId))
                            .count())
                    .sum();
            if (validCards < graveyardTarget.minTargets()) {
                return false;
            }
        }
        if (!triggerTargetCollector.hasLegalSpellTarget(gameData, option, pending.controllerId())) {
            return false;
        }
        boolean requiresTarget = option.minTargets() > 0 && option.effects().stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        if (!requiresTarget) {
            return true;
        }

        Permanent source = pending.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                option.effects(),
                option.targetFilter(),
                pending.controllerId(),
                pending.sourceCard(),
                TriggerTargetCollector.Options.ATTACK,
                source,
                defendingPlayerId(gameData, pending.attackedTargetId()));
        return !result.validTargets().isEmpty();
    }

    /**
     * Puts the chosen modes of {@code triggerRecord}'s ability, which the stack door parked for its mode, on the stack
     * as one object (CR 603.3c), or queues the choice of their targets (CR 603.3d), whose handler finishes the parked
     * ability. {@code attackedTargetId} is the attacked player or permanent the ability was about, or {@code null}.
     */
    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosenModes, UUID attackedTargetId,
            StackEntry triggerRecord) {
        if (!com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService
                .parkedAtDoor(triggerRecord)) {
            throw new IllegalStateException("A triggered ability's modes are chosen as the stack door parks it: "
                    + sourceCard.getName());
        }
        if (chosenModes.isEmpty()) {
            return;
        }
        List<CardEffect> effects = chosenModes.stream()
                .flatMap(option -> option.effects().stream())
                .toList();
        if (chosenModes.size() == 1 && effects.stream().anyMatch(EffectResolution::targetsSpellOnStack)) {
            // A mode that targets a spell ("return target spell you don't control", Hullbreaker Horror) chooses it on
            // the stack as the ability goes there (CR 603.3d), among the spells its stack filter admits.
            StackEntryPredicate spellFilter = TriggerTargetCollector.spellTargetFilter(chosenModes.getFirst());
            gameData.queueInteractionFirst(new PermanentChoiceContext.ETBSpellTargetTrigger(
                    sourceCard, controllerId, effects, spellFilter,
                    spellFilter != null && TriggerCollectionService.predicateContainsHasTarget(spellFilter),
                    sourcePermanentId).withTriggerRecord(triggerRecord));
            return;
        }
        boolean needsGraveyardTarget = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                        || graveyardTargetingSupport.findTarget(List.of(effect)) != null);
        boolean needsTarget = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD));
        if (needsGraveyardTarget && !needsTarget) {
            // An X in the chosen mode's target count is the ability's, from its record ("up to X target creature
            // cards", Orcus, Prince of Undeath).
            gameData.queueInteractionFirst(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, controllerId, effects, null, 0, triggerRecord.getXValue())
                    .withTriggerRecord(triggerRecord));
            return;
        }
        if (needsTarget) {
            Card targetingCard = prepareTriggeredModalTargeting(sourceCard, chosenModes);
            if (targetingCard.getTargetGroups().size() > 1
                    || etbTokenTargetService.needsSlotBySlotTargetSelection(targetingCard)) {
                gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        targetingCard, controllerId, effects, sourcePermanentId,
                        List.of(), 0, 0).withTriggerRecord(triggerRecord));
            } else {
                TargetFilter targetFilter = targetingCard.getTargetGroups().isEmpty()
                        ? null : targetingCard.getTargetGroups().getFirst().getFilter();
                if (attackedTargetId == null) {
                    gameData.queueInteractionFirst(new PermanentChoiceContext.EntersTriggerTarget(
                            targetingCard, controllerId, effects, sourcePermanentId, null, null, targetFilter)
                            .withTriggerRecord(triggerRecord));
                } else {
                    gameData.queueInteractionFirst(new PermanentChoiceContext.AttackTriggerTarget(targetingCard,
                            controllerId, effects, sourcePermanentId, controllerId, attackedTargetId)
                            .withTriggerRecord(triggerRecord)
                            .withEventValue(triggerRecord.getEventValue())
                            .withTriggeringPlayer(triggerRecord.getTriggeringPlayerId()));
                }
            }
            return;
        }

        // No chosen mode targets: the ability the stack door parked goes on the stack with the chosen modes' effects.
        triggeredAbilityStackService.finishParked(gameData, triggerRecord,
                chosen -> chosen.replaceEffectsToResolve(effects));
    }

    private Card prepareTriggeredModalTargeting(Card sourceCard,
                                                 List<ChooseOneEffect.ChooseOneOption> chosenModes) {
        Card targetingCard = sourceCard.createRuntimeCopy();
        targetingCard.clearRuntimeSpellTargets();
        targetingCard.setCastTimeTargetFilter(null);
        // Each chosen mode has its own target instruction. The same object may be chosen
        // for different modes, while repeated choices within one mode must stay distinct.
        if (chosenModes.size() > 1
                && sourceCard.getMultiTargetConstraint() != com.github.laxika.magicalvibes.model.MultiTargetConstraint.DISTINCT_TARGETS) {
            targetingCard.setAllowSharedTargets(true);
        }
        for (ChooseOneEffect.ChooseOneOption mode : chosenModes) {
            if (mode.targetFilters() != null) {
                for (int i = 0; i < mode.targetFilters().size(); i++) {
                    SpellTarget target = targetingCard.target(mode.targetFilters().get(i));
                    if (i < mode.effects().size()) {
                        targetingCard.registerEffectTargetIndex(mode.effects().get(i), target.getIndex());
                    }
                }
            } else if (mode.targetFilter() != null || needsFlatModalTargetGroup(mode.effects())) {
                SpellTarget target = declareTriggeredModalTarget(targetingCard, mode);
                for (CardEffect modeEffect : mode.effects()) {
                    targetingCard.registerEffectTargetIndex(modeEffect, target.getIndex());
                }
            }
        }
        return targetingCard;
    }

    private SpellTarget declareTriggeredModalTarget(Card card, ChooseOneEffect.ChooseOneOption mode) {
        if (mode.minTargets() != 1 || mode.maxTargets() != 1) {
            return card.target(mode.targetFilter(), mode.minTargets(), mode.maxTargets());
        }
        return card.target(mode.targetFilter());
    }

    private static boolean needsFlatModalTargetGroup(List<CardEffect> effects) {
        return effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD)
                        || EffectResolution.targetsSpellOnStack(effect));
    }

    /**
     * Lets players order the triggered abilities that went on the stack together, once no ability waits for a choice
     * ({@link #beginTriggerOrder}).
     */
    public void processNextSpellTargetTrigger(GameData gameData) {
        beginTriggerOrder(gameData);
    }

    /**
     * Lets players order the triggered abilities that went on the stack together (CR 603.3b): each
     * player, in APNAP order, whose new triggers include two or more non-equivalent abilities chooses
     * their order, unless that player lets the game order triggers automatically. Runs once no more
     * triggers are waiting to go on the stack, before a player receives priority. Every new trigger is
     * marked as ordered so it never joins a later batch.
     *
     * <p>First it drains the game's closed rules event batches ({@link GameData#drainRulesEvents}). The trigger
     * pipeline matched each of them as it closed; draining releases them and shows them to a test's shadow
     * recorder.
     *
     * @return true when an ordering prompt was opened
     */
    public boolean beginTriggerOrder(GameData gameData) {
        if (gameData.interaction.isAwaitingInput() || !gameData.pendingInteractions.isEmpty()
                || !gameData.pendingMayAbilities.isEmpty()) {
            return false;
        }
        gameData.drainRulesEvents();
        putNewTriggersInApnapOrder(gameData);
        for (UUID playerId : apnapOrder(gameData)) {
            List<StackEntry> batch = gameData.stack.stream()
                    .filter(entry -> isUnorderedTrigger(entry) && playerId.equals(entry.getControllerId()))
                    .toList();
            batch.forEach(entry -> entry.setTriggerOrderChosen(true));
            if (batch.size() < 2 || gameData.playersAutoOrderingTriggers.contains(playerId)
                    || batch.stream().allMatch(entry -> equivalentTriggers(batch.getFirst(), entry))) {
                continue;
            }
            List<UUID> ids = batch.stream().map(StackEntry::getTargetableId).toList();
            interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                    playerId, null, null,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.TriggerOrder(ids, List.of()),
                    java.util.stream.IntStream.range(0, ids.size()).mapToObj(index ->
                            (index + 1) + ": " + triggerOrderLabel(batch.get(index))).toList(),
                    "Choose the trigger to put on the stack first (it will resolve last)."));
            return true;
        }
        markTriggersOrdered(gameData);
        return false;
    }

    /**
     * CR 603.3b: triggers that went on the stack together are put there in APNAP order, so the active
     * player's go first (resolving last). The sort is stable, so each controller's own triggers keep
     * the order they were collected in until that player orders them.
     */
    private static void putNewTriggersInApnapOrder(GameData gameData) {
        List<UUID> order = apnapOrder(gameData);
        synchronized (gameData.stack) {
            int size = gameData.stack.size();
            int start = size;
            while (start > 0 && isUnorderedTrigger(gameData.stack.get(start - 1))) {
                start--;
            }
            if (size - start < 2) {
                return;
            }
            List<StackEntry> sorted = new ArrayList<>(gameData.stack.subList(start, size));
            sorted.sort(java.util.Comparator.comparingInt(entry -> {
                int index = order.indexOf(entry.getControllerId());
                return index < 0 ? order.size() : index;
            }));
            for (int i = 0; i < sorted.size(); i++) {
                gameData.stack.set(start + i, sorted.get(i));
            }
        }
    }

    /** Marks every triggered ability already on the stack as ordered, once a player has had priority. */
    public static void markTriggersOrdered(GameData gameData) {
        gameData.stack.stream().filter(TriggeredAbilityQueueService::isUnorderedTrigger)
                .forEach(entry -> entry.setTriggerOrderChosen(true));
    }

    private static boolean isUnorderedTrigger(StackEntry entry) {
        return entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && !entry.isTriggerOrderChosen() && !entry.isCopy();
    }

    /** Triggers whose order can't matter: the same ability of the same source with the same choices. */
    private static boolean equivalentTriggers(StackEntry first, StackEntry other) {
        return java.util.Objects.equals(first.getSourcePermanentId(), other.getSourcePermanentId())
                && first.getCard() != null && other.getCard() != null
                && first.getCard().getId().equals(other.getCard().getId())
                && first.getEffectsToResolve().equals(other.getEffectsToResolve())
                && java.util.Objects.equals(first.getTargetId(), other.getTargetId())
                && java.util.Objects.equals(first.getTargetIds(), other.getTargetIds())
                && java.util.Objects.equals(first.getTriggeringCardId(), other.getTriggeringCardId())
                && first.getXValue() == other.getXValue();
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }

    public static String triggerOrderLabel(StackEntry entry) {
        if (entry.getEffectsToResolve().stream().anyMatch(
                com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect.class::isInstance)) {
            return entry.getCard().getName() + " - sacrifice this creature";
        }
        if (entry.getEffectsToResolve().size() == 1
                && entry.getEffectsToResolve().getFirst()
                instanceof com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect boost
                && boost.powerBoost() instanceof com.github.laxika.magicalvibes.model.amount.Fixed power
                && boost.toughnessBoost() instanceof com.github.laxika.magicalvibes.model.amount.Fixed toughness) {
            return entry.getCard().getName() + " - target creature gets "
                    + (power.value() >= 0 ? "+" : "") + power.value() + "/"
                    + (toughness.value() >= 0 ? "+" : "") + toughness.value();
        }
        List<CardEffect> resolving = entry.getEffectsToResolve().size() == 1
                && entry.getEffectsToResolve().getFirst() instanceof MayEffect may
                ? List.of(may.wrapped()) : entry.getEffectsToResolve();
        // A "whenever you cast a [color] spell" ability, told apart by its color: the effects its collector put on the
        // stack.
        for (TriggeredAbility ability : entry.getCard().getTriggeredAbilities()) {
            for (TriggerCondition condition : ability.conditions()) {
                if (!(condition.trigger() instanceof SpellTrigger spell) || spell.filter() == null
                        || !spell.unfiltered().equals(Triggers.youCastSpell())
                        || !condition.zones().functionsIn(Zone.BATTLEFIELD)
                        || !(spell.filter().spell() instanceof com.github.laxika.magicalvibes.model.filter.CardColorPredicate color)) {
                    continue;
                }
                List<CardEffect> effects = CastAbility.of(ability, spell.filter()).effects();
                if (effects.equals(entry.getEffectsToResolve()) || effects.equals(resolving)) {
                    return entry.getDescription() + " — cast a "
                            + color.color().name().toLowerCase(java.util.Locale.ROOT) + " spell";
                }
            }
        }
        return entry.getDescription();
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        PermanentChoiceContext.SagaChapterTarget first =
                gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
        if (first != null && !first.targetGroups().isEmpty()) {
            processNextSagaChapterMultiTarget(gameData);
            return;
        }

        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            PermanentChoiceContext.SagaChapterTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
            if (!pending.targetGroups().isEmpty()) {
                processNextSagaChapterMultiTarget(gameData);
                return;
            }

            // Collect valid permanent targets, applying any saga target filter
            List<UUID> validPermanentTargets = collectSagaChapterTargets(gameData, pending);

            gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);

            if (validPermanentTargets.isEmpty() && sagaChapterTargetRequired(pending)) {
                // CR 603.3d: a required target with no legal choice means the ability is removed from the
                // stack, so the Saga (already at its final chapter) is sacrificed right away.
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s chapter " + pending.chapterName() + " has no valid targets and is removed from the stack."));
                log.info("Game {} - {} chapter {} has no legal target for its required target, removed",
                        gameData.id, pending.sourceCard().getName(), pending.chapterName());
                continue;
            }
            if (validPermanentTargets.isEmpty()) {
                // "Up to one" with no legal target: the ability goes on the stack with none chosen.
                triggeredAbilityStackService.finishParked(gameData, pending.triggerRecord(),
                        entry -> entry.setNonTargeting(false));
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s chapter " + pending.chapterName() + " has no valid targets."));
                log.info("Game {} - {} chapter {} no valid targets, pushed with null target",
                        gameData.id, pending.sourceCard().getName(), pending.chapterName());
                continue;
            }

            // "Up to one" — add controller player ID as a "skip" option
            var counterDistribution = sagaChapterCounterDistributionSupport
                    .findFixedDistribution(pending.effects());
            if (counterDistribution.isPresent()) {
                int maxTargets = Math.min(counterDistribution.get().total(), validPermanentTargets.size());
                gameData.interaction.setPermanentChoiceContext(pending);
                playerInputService.beginMultiPermanentChoice(
                        gameData,
                        pending.controllerId(),
                        validPermanentTargets,
                        maxTargets,
                        new com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext
                                .SagaChapterCounterDistribution(
                                pending.sourceCard(), pending.controllerId(), pending.effects(),
                                pending.sourcePermanentId(), pending.chapterName(),
                                counterDistribution.get().counterType(), counterDistribution.get().total(),
                                pending.triggerRecord()),
                        pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                                + " — Choose one, two, or three target creatures.");

                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s chapter " + pending.chapterName() + " - choose creatures for counter distribution."));
                log.info("Game {} - {} chapter {} awaiting counter distribution targets",
                        gameData.id, pending.sourceCard().getName(), pending.chapterName());
                return;
            }

            List<UUID> allChoices = new ArrayList<>(validPermanentTargets);
            boolean targetRequired = sagaChapterTargetRequired(pending);
            List<UUID> playerChoices = targetRequired ? List.of() : List.of(pending.controllerId());
            boolean creaturesOnly = sagaChapterTargetsCreaturesOnly(pending);
            String targetDescription = creaturesOnly ? "target creature" : "target permanent";

            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(), allChoices, playerChoices,
                    pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                            + " — Choose " + targetDescription + (targetRequired ? "." : ", or yourself to skip."));

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s chapter " + pending.chapterName() + " - choose " + targetDescription + "."));
            log.info("Game {} - {} chapter {} awaiting target selection", gameData.id, pending.sourceCard().getName(), pending.chapterName());
            return;
        }
    }

    public void processNextSagaChapterPlayerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class)) {
            PermanentChoiceContext.SagaChapterPlayerTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class);
            List<UUID> validPlayerTargets = collectSagaChapterPlayerTargets(gameData, pending);
            gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class);

            if (validPlayerTargets.isEmpty()) {
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s chapter " + pending.chapterName() + " has no valid player targets."));
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPlayerChoice(gameData, pending.controllerId(), validPlayerTargets,
                    pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                            + " - Choose target opponent.");
            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s chapter " + pending.chapterName() + " - choose target opponent."));
            log.info("Game {} - {} chapter {} awaiting player target selection",
                    gameData.id, pending.sourceCard().getName(), pending.chapterName());
            return;
        }
    }

    private List<UUID> collectSagaChapterPlayerTargets(
            GameData gameData, PermanentChoiceContext.SagaChapterPlayerTarget pending) {
        boolean opponentOnly = !pending.effects().isEmpty()
                && pending.effects().stream().allMatch(e -> e.targetPlayerRelation() == PlayerRelation.OPPONENT);
        boolean selfOnly = !pending.effects().isEmpty()
                && pending.effects().stream().allMatch(e -> e.targetPlayerRelation() == PlayerRelation.SELF);

        List<UUID> validTargets = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (opponentOnly && playerId.equals(pending.controllerId())) {
                continue;
            }
            if (selfOnly && !playerId.equals(pending.controllerId())) {
                continue;
            }
            boolean matchesFilters = pending.targetFilters().stream()
                    .filter(PlayerPredicateTargetFilter.class::isInstance)
                    .map(PlayerPredicateTargetFilter.class::cast)
                    .allMatch(filter -> targetLegalityService.matchesPlayerPredicate(
                            gameData, pending.controllerId(), playerId, filter.predicate()));
            if (!matchesFilters) {
                continue;
            }
            validTargets.add(playerId);
        }
        return validTargets;
    }

    private void processNextSagaChapterMultiTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            PermanentChoiceContext.SagaChapterTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
            if (pending.targetGroups().isEmpty()) {
                processNextSagaChapterTarget(gameData);
                return;
            }

            int groupIndex = pending.currentGroupIndex();
            if (groupIndex >= pending.targetGroups().size()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
                // Every group chosen: the ability goes on the stack with its targets, each judged by its group's
                // filter as it resolves.
                List<TargetFilter> groupFilters = null;
                if (pending.targetGroups().size() == 1) {
                    groupFilters = java.util.Collections.nCopies(
                            pending.chosenTargetsSoFar().size(), pending.targetGroups().getFirst().filter());
                } else if (pending.completedGroupTargetCounts().size() == pending.targetGroups().size()) {
                    List<TargetFilter> filters = new ArrayList<>();
                    for (int g = 0; g < pending.targetGroups().size(); g++) {
                        TargetFilter filter = pending.targetGroups().get(g).filter();
                        for (int k = 0; k < pending.completedGroupTargetCounts().get(g); k++) {
                            filters.add(filter);
                        }
                    }
                    if (filters.size() == pending.chosenTargetsSoFar().size()) {
                        groupFilters = filters;
                    }
                }
                List<TargetFilter> chosenFilters = groupFilters;
                triggeredAbilityStackService.finishParked(gameData, pending.triggerRecord(), entry -> {
                    entry.setNonTargeting(false);
                    entry.chooseTargetIds(new ArrayList<>(pending.chosenTargetsSoFar()));
                    if (chosenFilters != null) {
                        entry.setTargetFilters(chosenFilters);
                    }
                });
                continue;
            }

            SagaChapterTargetGroup group = pending.targetGroups().get(groupIndex);
            List<UUID> validTargets = collectSagaChapterTargetGroup(gameData, pending, group);
            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
                if (group.minTargets() > 0) {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s chapter " + pending.chapterName() + " has no valid targets."));
                    continue;
                }
                gameData.queueInteractionFirst(pending.withProgress(pending.chosenTargetsSoFar(), groupIndex + 1,
                        pending.closeCurrentGroup(pending.chosenTargetsSoFar().size())));
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            if (group.filter() instanceof PlayerPredicateTargetFilter) {
                int maxTargets = Math.min(group.maxTargets(), validTargets.size());
                gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
                playerInputService.beginMultiPermanentOrPlayerChoice(
                        gameData, pending.controllerId(), List.of(), validTargets, maxTargets,
                        new com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext
                                .SagaChapterTargetSelection(
                                pending.sourceCard(), pending.controllerId(), pending.effects(),
                                pending.sourcePermanentId(), pending.chapterName(), group.minTargets(),
                                pending.triggerRecord()),
                        pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                                + " - Choose any number of target players.");
                return;
            }

            if (group.filter() instanceof AnyTargetPredicateTargetFilter) {
                List<UUID> permanentTargets = validTargets.stream()
                        .filter(id -> !gameData.playerIds.contains(id))
                        .toList();
                List<UUID> playerTargets = validTargets.stream()
                        .filter(gameData.playerIds::contains)
                        .toList();
                playerInputService.beginAnyTargetChoice(
                        gameData, pending.controllerId(), permanentTargets, playerTargets,
                        pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                                + " - Choose target " + (groupIndex + 1) + ".");
                return;
            }

            boolean onePerControllerIfAble = pending.sourceCard().getMultiTargetConstraint()
                    == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE;
            boolean canSkip = !onePerControllerIfAble && (group.minTargets() == 0
                    || (pending.targetGroups().size() == 1
                    && pending.chosenTargetsSoFar().size() >= group.minTargets()));
            List<UUID> skipChoice = canSkip
                    ? List.of(pending.controllerId()) : List.of();
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(), validTargets, skipChoice,
                    pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                            + " - Choose target " + (groupIndex + 1) + ".");
            return;
        }
    }

    public void processNextSagaChapterGraveyardTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class)) {
            PermanentChoiceContext.SagaChapterGraveyardTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class);

            // Find the graveyard-targeting effect to extract its filter
            CardPredicate filter = null;
            int maxTargets = 1;
            int minTargets = 1;
            GraveyardSearchScope scope = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
            boolean anyNumber = false;
            int maxTotalManaValue = 0;
            for (CardEffect effect : pending.effects()) {
                if (effect instanceof GraveyardCardChoosingEffect choosingEffect
                        && choosingEffect.choosesGraveyardCards()) {
                    filter = choosingEffect.graveyardChoiceFilter();
                    maxTargets = choosingEffect.graveyardChoiceMaxTargets();
                    minTargets = 0;
                    scope = effect.targetSpec().graveyardScope().orElse(
                            choosingEffect.singleGraveyard()
                                    ? GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                                    : GraveyardSearchScope.ALL_GRAVEYARDS);
                    break;
                } else if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
                    filter = returnEffect.filter();
                    minTargets = returnEffect.upTo() ? 0 : 1;
                    scope = returnEffect.source();
                    break;
                } else if (effect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnEffect) {
                    filter = returnEffect.filter();
                    maxTargets = returnEffect.maxTargets();
                    minTargets = returnEffect.minTargets();
                    break;
                } else if (effect instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect) {
                    filter = returnEffect.filter();
                    minTargets = returnEffect.xScaled() ? 1 : returnEffect.minTargets();
                    if (returnEffect.hasTotalManaValueCap()) {
                        anyNumber = true;
                        maxTotalManaValue = returnEffect.maxTotalManaValue();
                        maxTargets = returnEffect.dynamicMaxTargets() == null && returnEffect.maxTargets() > 0
                                ? returnEffect.maxTargets()
                                : Integer.MAX_VALUE;
                    } else {
                        maxTargets = returnEffect.maxTargets();
                    }
                    scope = returnEffect.targetSpec().graveyardScope()
                            .orElse(GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
                    break;
                } else if (effect instanceof TargetedGraveyardCardsEffect targetEffect) {
                    filter = targetEffect.filter();
                    maxTargets = targetEffect.maxTargets() == 0
                            ? Integer.MAX_VALUE : targetEffect.maxTargets();
                    minTargets = 0;
                    scope = targetEffect.source();
                    break;
                } else if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    filter = effect.targetSpec().graveyardCardPredicate().orElse(null);
                    scope = effect.targetSpec().graveyardScope().orElse(scope);
                    break;
                }
            }

            List<Card> matchingCards = new ArrayList<>();
            boolean graveyardCardsTargetable = gameQueryService.canGraveyardCardsBeTargeted(gameData);
            for (UUID graveyardOwnerId : gameData.orderedPlayerIds) {
                if (scope == GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                        && !graveyardOwnerId.equals(pending.controllerId())) {
                    continue;
                }
                if (scope == GraveyardSearchScope.OPPONENT_GRAVEYARD
                        && graveyardOwnerId.equals(pending.controllerId())) {
                    continue;
                }
                List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
                if (graveyard != null && graveyardCardsTargetable) {
                    for (Card graveyardCard : graveyard) {
                        if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, null)
                                && (!anyNumber || graveyardCard.getManaValue() <= maxTotalManaValue)) {
                            matchingCards.add(graveyardCard);
                        }
                    }
                }
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class);

            if (matchingCards.isEmpty()) {
                if (minTargets == 0) {
                    pushSagaChapterGraveyardTriggeredAbility(gameData, pending);
                } else {
                    log.info("Game {} - {} chapter {} graveyard-target skipped (no valid targets)",
                            gameData.id, pending.sourceCard().getName(), pending.chapterName());
                }
                continue;
            }

            // Set up graveyard target operation (entryType = null → triggered ability path)
            gameData.graveyardTargetOperation.card = pending.sourceCard();
            gameData.graveyardTargetOperation.controllerId = pending.controllerId();
            gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());
            gameData.graveyardTargetOperation.anyNumber = anyNumber;
            gameData.graveyardTargetOperation.sourcePermanentId = pending.sourcePermanentId();
            gameData.graveyardTargetOperation.chapterName = pending.chapterName();
            // The chapter ability the stack door parked, which the answer finishes with the cards chosen.
            gameData.graveyardTargetOperation.triggerRecord = pending.triggerRecord();

            String filterLabel = CardPredicateUtils.describeFilter(filter);
            String prompt = pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                    + " — Choose target " + filterLabel + " from your graveyard.";
            if (anyNumber) {
                playerInputService.beginMultiGraveyardChoiceWithMaximumManaValue(
                        gameData, pending.controllerId(), matchingCards,
                        Math.min(maxTargets, matchingCards.size()), minTargets, maxTotalManaValue, prompt);
            } else {
                playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards,
                        Math.min(maxTargets, matchingCards.size()), minTargets, prompt);
            }

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s chapter " + pending.chapterName() + " ability triggers — choose a graveyard target."));
            log.info("Game {} - {} chapter {} graveyard-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName(), pending.chapterName());
            return;
        }
    }

    private void pushSagaChapterGraveyardTriggeredAbility(
            GameData gameData, PermanentChoiceContext.SagaChapterGraveyardTarget pending) {
        // "Up to" with none chosen: the chapter ability the stack door parked goes on the stack with no cards.
        triggeredAbilityStackService.finishParked(gameData, pending.triggerRecord(), entry -> {
            entry.setNonTargeting(false);
            entry.setTargetCardIds(new ArrayList<>());
        });
        gameLogService.append(gameData, GameLog.text(pending.sourceCard().getName() + "'s chapter "
                + pending.chapterName() + " ability triggers targeting no cards."));
    }

    public void processNextSpellGraveyardTargetTrigger(GameData gameData) {
        while (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            PermanentChoiceContext.SpellGraveyardTargetTrigger pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);

            if (pending.deferredTargeting() != null) {
                // A graveyard target choice that waited for another choice to be made: the method that queued it
                // begins it now, by its own rules.
                gameData.pollPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
                pending.deferredTargeting().accept(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
                continue;
            }

            ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect opponentChoiceEffect = pending.effects().stream()
                    .map(this::opponentChoiceEffect)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (opponentChoiceEffect != null) {
                if (beginOpponentChosenGraveyardTarget(gameData, pending, opponentChoiceEffect)) {
                    return;
                }
                continue;
            }

            IndependentlyTargetedGraveyardCardsEffect independentTargetEffect =
                    EffectResolution.expandConditionalTargetingEffects(pending.effects()).stream()
                            .filter(IndependentlyTargetedGraveyardCardsEffect.class::isInstance)
                            .map(IndependentlyTargetedGraveyardCardsEffect.class::cast)
                            .findFirst()
                            .orElse(null);
            if (independentTargetEffect != null) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
                var operation = gameData.graveyardTargetOperation;
                operation.card = pending.sourceCard();
                operation.controllerId = pending.controllerId();
                operation.effects = new ArrayList<>(pending.effects());
                operation.xValue = pending.xValue();
                operation.sourceAlternateCostAtTrigger = pending.sourceAlternateCostAtTrigger();
                operation.triggeringPermanentPowerAtTrigger = pending.sourcePowerAtTrigger();
                operation.triggeringPermanentId = pending.triggeringPermanentId();
                operation.kicked = false;
                operation.independentTargetGroupIndex = 0;
                operation.independentTargetCardIds.clear();
                operation.independentTargetGroupSizes.clear();
                operation.triggerRecord = pending.triggerRecord();

                boolean kicked = false;
                List<Permanent> battlefield = gameData.playerBattlefields.get(pending.controllerId());
                if (battlefield != null) {
                    for (Permanent permanent : battlefield) {
                        if (permanent.getCard().getId().equals(pending.sourceCard().getId())) {
                            operation.sourcePermanentId = permanent.getId();
                            kicked = permanent.isKicked();
                            operation.kicked = kicked;
                            break;
                        }
                    }
                }

                try {
                    if (graveyardTargetingService.beginIndependentGraveyardSpellTargeting(
                            gameData, pending.controllerId(), independentTargetEffect, kicked)) {
                        return;
                    }
                } catch (IllegalStateException noLegalRequiredTarget) {
                    operation.card = null;
                    operation.controllerId = null;
                    operation.effects = null;
                    operation.sourcePermanentId = null;
                    operation.independentTargetGroupIndex = -1;
                    operation.independentTargetCardIds.clear();
                    operation.independentTargetGroupSizes.clear();
                    operation.triggerRecord = null;
                    log.info("Game {} - {} graveyard-target trigger skipped (no valid required target)",
                            gameData.id, pending.sourceCard().getName());
                    continue;
                }

                pushSpellGraveyardTriggeredAbilityWithoutTargets(gameData, pending);
                continue;
            }

            // Find the graveyard-targeting effect to extract its filter and search scope
            CardPredicate filter = null;
            boolean lifeGainedCap = false;
            boolean lifeLostCap = false;
            boolean manaValueEqualsX = false;
            int manaValueXOffset = 0;
            boolean manaValueAtMostX = false;
            GraveyardSearchScope scope = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
            GraveyardTargetingSupport.Target describedTarget =
                    graveyardTargetingSupport.findTarget(pending.effects());
            ReturnUpToOneOfEachFilterFromGraveyardToHandEffect oneOfEachFilterEffect = pending.effects().stream()
                    .filter(ReturnUpToOneOfEachFilterFromGraveyardToHandEffect.class::isInstance)
                    .map(ReturnUpToOneOfEachFilterFromGraveyardToHandEffect.class::cast)
                    .findFirst()
                    .orElse(null);
            ReturnCardFromGraveyardEffect returnEffect = pending.effects().stream()
                    .map(this::targetedReturnEffect)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (oneOfEachFilterEffect != null) {
                scope = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
            } else if (returnEffect != null) {
                filter = returnEffect.filter();
                lifeGainedCap = returnEffect.maxManaValueEqualsLifeGainedThisTurn();
                lifeLostCap = returnEffect.maxManaValueEqualsLifeLostThisTurn();
                manaValueEqualsX = returnEffect.requiresManaValueEqualsX();
                manaValueXOffset = returnEffect.manaValueXOffset();
                manaValueAtMostX = returnEffect.requiresManaValueAtMostX()
                        || returnEffect.dynamicMaxManaValue() instanceof ManaSpentToCast;
                scope = returnEffect.source();
            } else if (describedTarget != null) {
                filter = describedTarget.filter();
                scope = describedTarget.scope();
            } else {
                for (CardEffect effect : pending.effects()) {
                    CardEffect targetEffect = unwrapConditionalEffect(effect);
                    if (targetEffect instanceof com.github.laxika.magicalvibes.model.effect.ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect
                            && pending.sourceCard().getEffectTargetIndex(effect) >= 0
                            && pending.sourceCard().getTargetFilter() instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                        filter = graveyardFilter.predicate();
                        scope = graveyardFilter.scope();
                        break;
                    }
                    if (targetEffect.targetSpec().graveyardScope().orElse(null) == GraveyardSearchScope.ALL_GRAVEYARDS) {
                        // BecomeAuraReanimateFromGraveyardEffect (Necromancy): creature card from any graveyard
                        filter = new CardTypePredicate(CardType.CREATURE);
                        scope = GraveyardSearchScope.ALL_GRAVEYARDS;
                        break;
                    }
                    if (targetEffect instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect steal) {
                        // Ink-Eyes, Servant of Oni: creature card from an opponent's graveyard, narrowed
                        // to the damaged player by the pending trigger's graveyardOwnerId.
                        filter = steal.filter();
                        scope = GraveyardSearchScope.OPPONENT_GRAVEYARD;
                        break;
                    }
                    if (targetEffect.targetSpec().declaredTarget() instanceof TargetPredicate.GraveyardCards graveyardCards) {
                        filter = graveyardCards.inner();
                        scope = graveyardCards.scope();
                        break;
                    }
                    GraveyardSearchScope declaredScope = targetEffect.targetSpec().graveyardScope().orElse(null);
                    if (declaredScope != null) {
                        scope = declaredScope;
                        break;
                    }
                }
            }
            if ((filter == null || filter instanceof CardTruePredicate)
                    && pending.sourceCard().getTargetFilter() instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                filter = graveyardFilter.predicate();
                scope = graveyardFilter.scope();
            }
            // "Target card with the same name as that spell" (Harness the Storm) is chosen against the spell the
            // ability triggered on, as it goes on the stack (CR 603.3d).
            filter = AmountEvaluationService.bindTriggeringSpellName(gameData, filter, null, pending.triggeringSpell());
            StackEntry triggeringCardRecord = gameData.queuedTriggerRecord(pending) != null
                    ? gameData.queuedTriggerRecord(pending) : pending.triggerRecord();
            // "Other than it" (Illicit Masquerade): the card the event was about.
            filter = AmountEvaluationService.bindTriggeringCard(filter,
                    triggeringCardRecord == null ? null : triggeringCardRecord.getTriggeringCardId());
            // A target restricted by the event ("mana value equal to the damage dealt", "lesser mana value" than the
            // sacrificed creature) is chosen against the record of the event, the entry the collector built for the
            // ability that chooses nothing, as it is judged against the entry as the ability resolves; the choice's
            // X is the ability's own.
            StackEntry eventRecord = gameData.queuedTriggerRecord(pending) != null
                    ? gameData.queuedTriggerRecord(pending) : pending.triggerRecord();
            Integer eventValue = eventRecord == null ? null : eventRecord.getEventValue();
            Integer aggregateManaValueCap = pending.graveyardTargetMaxManaValue() != null
                    ? pending.graveyardTargetMaxManaValue()
                    : describedTarget != null && describedTarget.maximumManaValue() != null
                    && !(describedTarget.maximumManaValue() instanceof com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures totalPower
                    && totalPower.declaredAttackersOnly())
                    ? amountEvaluationService.evaluate(gameData, describedTarget.maximumManaValue(),
                    eventRecord == null
                            ? new AmountContext(pending.controllerId(), null, null,
                                    pending.xValue(), pending.xValue())
                            : new AmountContext(pending.controllerId(), null, null,
                                    pending.xValue(), eventRecord.getEventValue(), false, null, List.of(), null,
                                    eventRecord, null, null, 0, 0, List.of()))
                    : null;
            // "mana value X or less, where X is the life you gained or lost this turn"
            int maxManaValue = aggregateManaValueCap != null ? aggregateManaValueCap
                    : lifeGainedCap
                    ? gameData.getLifeGainedThisTurn(pending.controllerId())
                    : lifeLostCap
                    ? gameData.lifeLostThisTurn.getOrDefault(pending.controllerId(), 0)
                    : manaValueAtMostX ? pending.xValue() + manaValueXOffset : Integer.MAX_VALUE;

            List<UUID> searchPlayerIds = pending.graveyardOwnerId() != null
                    ? List.of(pending.graveyardOwnerId())
                    : scope.graveyardOwners(gameData.orderedPlayerIds, pending.controllerId());

            // "Another target card" is judged against the source's own physical card: for a copy that left the
            // battlefield (Phantasmal Image copying Gixian Puppeteer) the copy's card, not the copied one.
            Permanent sourceSnapshot = pending.triggerRecord() == null ? null
                    : pending.triggerRecord().getSourcePermanentSnapshot();
            UUID selfCardId = sourceSnapshot != null && sourceSnapshot.getOriginalCard() != null
                    ? sourceSnapshot.getOriginalCard().getId() : pending.sourceCard().getId();
            List<Card> matchingCards = new ArrayList<>();
            for (UUID playerId : searchPlayerIds) {
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                if (graveyard == null || !gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
                    continue;
                }
                for (Card graveyardCard : graveyard) {
                    if (gameQueryService.isLandCardTargetRestricted(gameData, graveyardCard,
                            pending.controllerId())) continue;
                    if (returnEffect != null && returnEffect.targetPutIntoGraveyardFromBattlefieldThisTurn()
                            && !gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn
                                    .getOrDefault(playerId, Set.of()).contains(graveyardCard.getId())) {
                        continue;
                    }
                    if (returnEffect != null && returnEffect.targetNotPutIntoGraveyardThisCombat()
                            && gameData.cardsPutIntoGraveyardThisCombat
                                    .getOrDefault(playerId, Set.of()).contains(graveyardCard.getId())) {
                        continue;
                    }
                    if (returnEffect != null && returnEffect.targetMilledThisTurn()
                            && !gameData.cardsMilledThisTurn
                                    .getOrDefault(playerId, Set.of()).contains(graveyardCard.getId())) {
                        continue;
                    }
                    if (returnEffect != null && returnEffect.targetPutIntoGraveyardFromAnywhereThisTurn()
                            && !gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                                    .getOrDefault(playerId, Set.of()).contains(graveyardCard.getId())) {
                        continue;
                    }
                    if (returnEffect != null && returnEffect.targetPutIntoGraveyardFromLibraryThisTurn()
                            && !gameData.cardsPutIntoGraveyardFromLibraryThisTurn
                                    .getOrDefault(playerId, Set.of()).contains(graveyardCard.getId())) {
                        continue;
                    }
                    if (returnEffect != null
                            && returnEffect.targetDiscardedOrPutIntoGraveyardFromLibraryThisTurn()
                            && !gameData.cardsDiscardedOrCycledThisTurn
                                    .getOrDefault(playerId, Set.of()).contains(graveyardCard.getId())
                            && !gameData.cardsPutIntoGraveyardFromLibraryThisTurn
                                    .getOrDefault(playerId, Set.of()).contains(graveyardCard.getId())) {
                        continue;
                    }
                    if (manaValueEqualsX
                            && graveyardCard.getManaValue() != pending.xValue() + manaValueXOffset) {
                        continue;
                    }
                    if (graveyardCard.getManaValue() > maxManaValue) {
                        continue;
                    }
                    boolean matches = oneOfEachFilterEffect != null
                            ? oneOfEachFilterEffect.filters().stream().anyMatch(candidateFilter ->
                            predicateEvaluationService.matchesCardPredicate(
                                    graveyardCard, candidateFilter, selfCardId, gameData, playerId,
                                    null, pending.sourcePowerAtTrigger()))
                            // An X in the filter is the choice's X; the event's value is the record's.
                            : filter == null || predicateEvaluationService.matchesCardPredicate(
                            graveyardCard, filter, selfCardId, gameData, playerId,
                            null, pending.sourcePowerAtTrigger(), pending.xValue(), null, eventValue);
                    if (matches) {
                        matchingCards.add(graveyardCard);
                    }
                }
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);

            int declaredMinTargets = declaredMinimumTargetCount(pending.sourceCard(), pending.effects());
            int describedMinTargets = returnEffect != null && returnEffect.upTo()
                    || pending.effects().stream().anyMatch(candidate ->
                            unwrapConditionalEffect(candidate) instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect steal
                                    && steal.upTo())
                    ? 0 : declaredMinTargets >= 0
                            ? declaredMinTargets : describedTarget == null ? 0 : describedTarget.minTargets();
            int minTargets = Math.max(pending.minCount(), describedMinTargets);
            if (matchingCards.isEmpty() && minTargets == 0) {
                pushSpellGraveyardTriggeredAbilityWithoutTargets(gameData, pending);
                continue;
            }
            if (matchingCards.size() < minTargets) {
                log.info("Game {} - {} spell-cast graveyard-target trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            // Set up graveyard target operation (entryType = null → triggered ability path)
            gameData.graveyardTargetOperation.card = pending.sourceCard();
            gameData.graveyardTargetOperation.controllerId = pending.controllerId();
            gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());
            gameData.graveyardTargetOperation.xValue = pending.xValue();
            gameData.graveyardTargetOperation.totalManaValueCap = aggregateManaValueCap;
            gameData.graveyardTargetOperation.graveyardTargetMaxManaValue = pending.graveyardTargetMaxManaValue();
            gameData.graveyardTargetOperation.singleGraveyard =
                    describedTarget != null && describedTarget.singleGraveyard();
            gameData.graveyardTargetOperation.sourceAlternateCostAtTrigger =
                    pending.sourceAlternateCostAtTrigger();
            gameData.graveyardTargetOperation.triggeringPermanentPowerAtTrigger =
                    pending.sourcePowerAtTrigger();
            gameData.graveyardTargetOperation.triggeringPermanentId = pending.triggeringPermanentId();
            gameData.graveyardTargetOperation.triggeringSpell = pending.triggeringSpell() == null
                    ? null : new StackEntry(pending.triggeringSpell());
            gameData.graveyardTargetOperation.triggerRecord = pending.triggerRecord();
            gameData.graveyardTargetOperation.attackingPermanentSnapshots = pending.attackingPermanentSnapshots();
            gameData.graveyardTargetOperation.sourcePermanentId = pending.sourcePermanentId();
            // Cards chosen from the one graveyard the choice was narrowed to ("up to two target creature cards from
            // defending player's graveyard"): that player is the entry's target, whose graveyard the cards must still
            // be in as the ability resolves.
            if (gameData.graveyardTargetOperation.effects.stream().anyMatch(effect ->
                    effect.graveyardCardChoice() != null
                    && !effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
                gameData.graveyardTargetOperation.targetPlayerId = pending.graveyardOwnerId();
            }
            gameData.graveyardTargetOperation.onceEachTurnActionKey =
                    pending.onceEachTurnActionKey();
            if (gameData.graveyardTargetOperation.sourcePermanentId == null) {
                // ETB source permanent (for intervening-if / attach); find by card id on the controller's BF
                List<Permanent> bf = gameData.playerBattlefields.get(pending.controllerId());
                if (bf != null) {
                    for (Permanent p : bf) {
                        if (p.getCard().getId().equals(pending.sourceCard().getId())) {
                            gameData.graveyardTargetOperation.sourcePermanentId = p.getId();
                            break;
                        }
                    }
                }
            }

            TargetGroup declaredGroup = targetGroupForTriggeredEffects(pending.sourceCard(), pending.effects());
            int requestedMaxTargets = oneOfEachFilterEffect != null
                    ? oneOfEachFilterEffect.filters().size()
                    : pending.maxCount() > 0
                    ? pending.maxCount()
                    : describedTarget == null ? 1
                    : describedTarget.maximumTargetCount() != null
                    ? Math.max(0, amountEvaluationService.evaluate(gameData,
                    describedTarget.maximumTargetCount(),
                    // "Up to that many" (Loamcrafter Faun): the count the event recorded.
                    new AmountContext(pending.controllerId(), null, null, pending.xValue(),
                            eventValue == null ? 0 : eventValue)))
                    : describedTarget.maxTargets();
            if (oneOfEachFilterEffect == null && pending.maxCount() == 0 && declaredGroup != null
                    && (describedTarget == null || describedTarget.maximumTargetCount() == null)) {
                requestedMaxTargets = declaredGroup.getMaxTargets();
            }
            if (requestedMaxTargets <= 0) {
                pushSpellGraveyardTriggeredAbilityWithoutTargets(gameData, pending);
                continue;
            }

            String filterLabel = oneOfEachFilterEffect != null && !oneOfEachFilterEffect.filters().isEmpty()
                    ? CardPredicateUtils.describeFilter(oneOfEachFilterEffect.filters().getFirst())
                    : CardPredicateUtils.describeFilter(filter);
            String zoneLabel = pending.graveyardOwnerId() != null ? "that player's graveyard" : switch (scope) {
                case ALL_GRAVEYARDS -> "a graveyard";
                case OPPONENT_GRAVEYARD -> "an opponent's graveyard";
                case CONTROLLERS_GRAVEYARD -> "your graveyard";
            };
            int maxTargets = Math.min(requestedMaxTargets, matchingCards.size());
            if (pending.sourceCard().getMultiTargetConstraint() == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE) {
                int graveyardCount = (int) matchingCards.stream()
                        .map(candidate -> gameQueryService.findGraveyardOwnerById(gameData, candidate.getId()))
                        .filter(java.util.Objects::nonNull).distinct().count();
                maxTargets = Math.min(maxTargets, graveyardCount);
                minTargets = maxTargets;
            }
            String countLabel = maxTargets == 1 ? "target " : minTargets == maxTargets
                    ? maxTargets + " target " : "up to " + maxTargets + " target ";
            String prompt = pending.sourceCard().getName() + "'s ability — Choose " + countLabel + filterLabel
                    + (maxTargets == 1 ? "" : "s") + " from " + zoneLabel + ".";
            // "Any number of target creature cards with total power 10 or less" (Nethroi, Apex of Death).
            Integer maxTotalPower = pending.effects().stream()
                    .map(CardEffect::graveyardCardChoice)
                    .filter(java.util.Objects::nonNull)
                    .map(GraveyardCardChoosingEffect::graveyardChoiceMaxTotalPower)
                    .filter(java.util.Objects::nonNull)
                    .findFirst().orElse(null);
            if (aggregateManaValueCap != null) {
                prompt = prompt.substring(0, prompt.length() - 1)
                        + " with total mana value " + aggregateManaValueCap + " or less.";
                playerInputService.beginMultiGraveyardChoiceWithMaximumManaValue(
                        gameData, pending.controllerId(), matchingCards, maxTargets, minTargets,
                        aggregateManaValueCap, prompt);
            } else if (maxTotalPower != null) {
                playerInputService.beginMultiGraveyardChoiceWithMaximumPower(gameData, pending.controllerId(),
                        matchingCards, maxTargets, minTargets, maxTotalPower, prompt);
            } else {
                playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards,
                        maxTargets, minTargets, prompt);
            }

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability triggers — choose a graveyard target."));
            log.info("Game {} - {} spell-cast graveyard-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    private CardEffect unwrapConditionalEffect(CardEffect effect) {
        while (effect instanceof ConditionalEffect conditional) {
            effect = conditional.wrapped();
        }
        return effect;
    }

    private int declaredMinimumTargetCount(Card card, List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof SequenceEffect sequence) {
                int nestedMinimum = declaredMinimumTargetCount(card, sequence.steps());
                if (nestedMinimum >= 0) return nestedMinimum;
            }
            int targetIndex = card.getEffectTargetIndex(effect);
            if (targetIndex >= 0 && targetIndex < card.getTargetGroups().size()) {
                TargetGroup target = card.getTargetGroups().get(targetIndex);
                return target.getMinTargets();
            }
        }
        if (card.getTargetGroups().size() == 1
                && card.getTargetGroups().getFirst().getFilter() instanceof GraveyardCardPredicateTargetFilter) {
            return card.getTargetGroups().getFirst().getMinTargets();
        }
        return -1;
    }

    /** "Up to" graveyard targets with none to choose: the ability the stack door parked goes on the stack with none. */
    private void pushSpellGraveyardTriggeredAbilityWithoutTargets(
            GameData gameData, PermanentChoiceContext.SpellGraveyardTargetTrigger pending) {
        List<CardEffect> effects = pending.effects();
        triggeredAbilityStackService.finishParked(gameData, pending.triggerRecord(), entry -> {
            entry.replaceEffectsToResolve(effects);
            entry.setTargetId(null);
            entry.setNonTargeting(true);
        });
        gameLogService.append(gameData, GameLog.text(pending.sourceCard().getName()
                + "'s ability triggers targeting no cards."));
    }

    private ReturnCardFromGraveyardEffect targetedReturnEffect(CardEffect effect) {
        if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
            return returnEffect;
        }
        if (effect instanceof MayEffect may) {
            return targetedReturnEffect(may.wrapped());
        }
        if (effect instanceof ConditionalEffect conditional) {
            return targetedReturnEffect(conditional.wrapped());
        }
        if (effect instanceof MayEffect may) {
            return targetedReturnEffect(may.wrapped());
        }
        if (effect instanceof MayPayManaEffect mayPay) {
            return targetedReturnEffect(mayPay.wrapped());
        }
        if (effect instanceof SacrificePermanentThenEffect sacrificeThen) {
            return targetedReturnEffect(sacrificeThen.thenEffect());
        }
        if (effect instanceof SacrificeSelfThenEffect sacrificeSelfThen) {
            return targetedReturnEffect(sacrificeSelfThen.thenEffect());
        }
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream()
                    .map(this::targetedReturnEffect)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private ReturnTargetCardsFromGraveyardToHandEffect targetedReturnToHandEffect(CardEffect effect) {
        if (effect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnEffect
                && returnEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            return returnEffect;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return targetedReturnToHandEffect(conditional.wrapped());
        }
        if (effect instanceof MayEffect may) {
            return targetedReturnToHandEffect(may.wrapped());
        }
        if (effect instanceof MayPayManaEffect mayPay) {
            return targetedReturnToHandEffect(mayPay.wrapped());
        }
        if (effect instanceof SacrificePermanentThenEffect sacrificeThen) {
            return targetedReturnToHandEffect(sacrificeThen.thenEffect());
        }
        if (effect instanceof SacrificeSelfThenEffect sacrificeSelfThen) {
            return targetedReturnToHandEffect(sacrificeSelfThen.thenEffect());
        }
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream()
                    .map(this::targetedReturnToHandEffect)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect opponentChoiceEffect(CardEffect effect) {
        if (effect instanceof ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect opponentChoice) {
            return opponentChoice;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return opponentChoiceEffect(conditional.wrapped());
        }
        if (effect instanceof MayEffect may) {
            return opponentChoiceEffect(may.wrapped());
        }
        return null;
    }

    private boolean beginOpponentChosenGraveyardTarget(GameData gameData,
                                                       PermanentChoiceContext.SpellGraveyardTargetTrigger pending,
                                                       ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect effect) {
        List<Card> matchingCards = new ArrayList<>();
        if (gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            List<Card> graveyard = gameData.playerGraveyards.get(pending.controllerId());
            if (graveyard != null) {
                for (Card graveyardCard : graveyard) {
                    if (predicateEvaluationService.matchesCardPredicate(
                            graveyardCard, effect.filter(), pending.sourceCard().getId(), gameData,
                            pending.controllerId())) {
                        matchingCards.add(graveyardCard);
                    }
                }
            }
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(pending.controllerId()))
                .toList();
        gameData.pollPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);

        if (matchingCards.isEmpty() || opponents.isEmpty()) {
            log.info("Game {} - {} opponent-chosen graveyard target skipped", gameData.id,
                    pending.sourceCard().getName());
            return false;
        }

        UUID sourcePermanentId = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(pending.controllerId());
        if (battlefield != null) {
            sourcePermanentId = battlefield.stream()
                    .filter(permanent -> permanent.getCard().getId().equals(pending.sourceCard().getId()))
                    .map(Permanent::getId)
                    .findFirst()
                    .orElse(null);
        }

        gameData.graveyardTargetOperation.card = pending.sourceCard();
        gameData.graveyardTargetOperation.controllerId = pending.controllerId();
        gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());
        gameData.graveyardTargetOperation.xValue = pending.xValue();
        gameData.graveyardTargetOperation.sourceAlternateCostAtTrigger =
                pending.sourceAlternateCostAtTrigger();
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;
        gameData.graveyardTargetOperation.triggerRecord = pending.triggerRecord();

        if (opponents.size() == 1) {
            returnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler.beginCardChoice(
                    gameData, opponents.getFirst(), matchingCards, pending.sourceCard().getName());
        } else {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.MausoleumTurnkeyOpponentChoice(
                            pending.sourceCard(), pending.controllerId(), pending.effects(), sourcePermanentId,
                            matchingCards));
            playerInputService.beginPlayerChoice(gameData, pending.controllerId(), opponents,
                    pending.sourceCard().getName() + "'s ability — choose an opponent to choose a creature card.");
        }

        gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                "'s ability triggers — an opponent chooses a creature card from its controller's graveyard."));
        log.info("Game {} - {} opponent-chosen graveyard target awaiting selection", gameData.id,
                pending.sourceCard().getName());
        return true;
    }

    /**
     * Collects valid permanent targets for a saga chapter, applying any target predicate
     * declared by the chapter's effects and any chapter-level target filters.
     */
    private List<UUID> collectSagaChapterTargets(GameData gameData,
                                                  PermanentChoiceContext.SagaChapterTarget pending) {
        // Extract target predicate from the first targeting effect that declares one
        PermanentPredicate targetPredicate = pending.effects().stream()
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        && EffectResolution.targetPredicateOf(e) != null)
                .map(EffectResolution::targetPredicateOf)
                .findFirst().orElse(null);

        // Also check for chapter-level target filters (e.g. "creature an opponent controls")
        Set<TargetFilter> chapterFilters = pending.targetFilters();
        boolean hasChapterFilters = chapterFilters != null && !chapterFilters.isEmpty();

        FilterContext filterContext = (targetPredicate != null || hasChapterFilters)
                ? FilterContext.of(gameData)
                        .withSourceCardId(pending.sourceCard().getId())
                        .withSourceControllerId(pending.controllerId())
                : null;

        boolean creaturesOnly = targetPredicate == null && !hasChapterFilters;
        List<UUID> validPermanentTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (creaturesOnly && !gameQueryService.isCreature(gameData, p)) continue;
                if (targetPredicate != null
                        && !predicateEvaluationService.matchesPermanentPredicate(p, targetPredicate, filterContext)) {
                    continue;
                }
                if (hasChapterFilters
                        && !predicateEvaluationService.matchesFilters(p, chapterFilters, filterContext)) {
                    continue;
                }
                if (validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, p, pending.controllerId())) {
                    continue;
                }
                validPermanentTargets.add(p.getId());
            }
        }
        return validPermanentTargets;
    }

    private boolean sagaChapterTargetRequired(PermanentChoiceContext.SagaChapterTarget pending) {
        int chapter = switch (pending.chapterName()) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            default -> 0;
        };
        return pending.sourceCard().isSagaChapterTargetRequired(chapter);
    }

    private boolean sagaChapterTargetsCreaturesOnly(PermanentChoiceContext.SagaChapterTarget pending) {
        return pending.effects().stream()
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                .map(EffectResolution::targetPredicateOf)
                .allMatch(java.util.Objects::isNull);
    }

    /**
     * The card's per-controller constraint describes a single "for each player, up to one target" phrase, so it
     * only applies to chapters with one target group. Chapters with several groups (such as "target Mutant fights
     * target creature named Fenric") target each group independently.
     */
    private static MultiTargetConstraint sagaChapterPerControllerConstraint(PermanentChoiceContext.SagaChapterTarget pending) {
        return pending.targetGroups().size() == 1 ? pending.sourceCard().getMultiTargetConstraint() : null;
    }

    private List<UUID> collectSagaChapterTargetGroup(GameData gameData,
                                                      PermanentChoiceContext.SagaChapterTarget pending,
                                                      SagaChapterTargetGroup group) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(pending.sourceCard().getId())
                .withSourceControllerId(pending.controllerId());
        List<UUID> validTargets = new ArrayList<>();
        if (group.filter() instanceof PlayerPredicateTargetFilter playerFilter) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (pending.chosenTargetsSoFar().contains(playerId)) {
                    continue;
                }
                if (sagaChapterPerControllerConstraint(pending) == MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER
                        && sagaChapterTargetControllerAlreadyChosen(
                        pending.chosenTargetsSoFar(), playerId, gameData)) {
                    continue;
                }
                if (targetLegalityService.matchesPlayerPredicate(
                        gameData, pending.controllerId(), playerId, playerFilter.predicate())) {
                    validTargets.add(playerId);
                }
            }
            return validTargets;
        }

        if (group.filter() instanceof AnyTargetPredicateTargetFilter anyFilter) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!pending.chosenTargetsSoFar().contains(playerId)
                        && targetLegalityService.matchesPlayerPredicate(
                        gameData, pending.controllerId(), playerId, anyFilter.playerPredicate())) {
                    validTargets.add(playerId);
                }
            }
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (!pending.chosenTargetsSoFar().contains(permanent.getId())
                            && predicateEvaluationService.matchesPermanentPredicate(
                            permanent, anyFilter.permanentPredicate(), filterContext)
                            && !validTargetService.isBlockedByHexproofOrGrantedEffect(
                            gameData, permanent, pending.controllerId())) {
                        validTargets.add(permanent.getId());
                    }
                }
            }
            return validTargets;
        }

        MultiTargetConstraint perControllerConstraint = sagaChapterPerControllerConstraint(pending);
        Set<UUID> chosenControllers = perControllerConstraint == MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER
                || perControllerConstraint == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE
                ? pending.chosenTargetsSoFar().stream()
                .map(id -> gameQueryService.findPermanentController(gameData, id))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet())
                : Set.of();
        List<Permanent> sharedTypeReference = pending.sourceCard().getMultiTargetConstraint()
                == MultiTargetConstraint.SHARE_CARD_TYPE
                ? pending.chosenTargetsSoFar().stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .toList()
                : List.of();

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (pending.chosenTargetsSoFar().contains(permanent.getId())) {
                    continue;
                }
                if (chosenControllers.contains(pid)) {
                    continue;
                }
                if (!sharedTypeReference.isEmpty() && sharedTypeReference.stream()
                        .noneMatch(sel -> gameQueryService.sharesCardType(gameData, sel, permanent))) {
                    continue;
                }
                if (group.filter() == null && !gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (group.filter() != null
                        && !predicateEvaluationService.matchesFilters(permanent, Set.of(group.filter()), filterContext)) {
                    continue;
                }
                if (validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, permanent, pending.controllerId())) {
                    continue;
                }
                if (group.maxTotalManaValue() < Integer.MAX_VALUE
                        && sagaChapterTargetManaValue(gameData, pending) + permanent.getCard().getManaValue()
                        > group.maxTotalManaValue()) {
                    continue;
                }
                validTargets.add(permanent.getId());
            }
        }
        return validTargets;
    }

    private boolean sagaChapterTargetControllerAlreadyChosen(List<UUID> chosenTargets,
                                                               UUID controllerId,
                                                               GameData gameData) {
        for (UUID chosenTarget : chosenTargets) {
            UUID chosenController = gameData.playerIdToName.containsKey(chosenTarget)
                    ? chosenTarget
                    : gameQueryService.findPermanentController(gameData, chosenTarget);
            if (controllerId.equals(chosenController)) {
                return true;
            }
        }
        return false;
    }

    private int sagaChapterTargetManaValue(GameData gameData,
                                            PermanentChoiceContext.SagaChapterTarget pending) {
        return pending.chosenTargetsSoFar().stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .mapToInt(permanent -> permanent.getCard().getManaValue())
                .sum();
    }
}
