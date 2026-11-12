package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TargetGroup;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a continuation by placing its effect on the stack as a reflexive ability. */
@Component
@RequiredArgsConstructor
public class QueueReflexiveAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final ETBTokenTargetService etbTokenTargetService;
    private final GameQueryService gameQueryService;

    @Autowired
    @Lazy
    private TargetLegalityService targetLegalityService;

    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

    @Autowired
    @Lazy
    private ValidTargetService validTargetService;

    @Autowired
    @Lazy
    private TriggeredAbilityQueueService triggeredAbilityQueueService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return QueueReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolve(gameData, entry, (QueueReflexiveAbilityEffect) effect, record -> { });
    }

    /**
     * Puts the reflexive ability on the stack with what the preceding action of the resolution determined about it
     * set on its record by {@code recordSetup} ("that player", "the tapped creature"), before the ability takes its
     * route: its target is chosen against the record, and the entry that reaches the stack carries it.
     */
    public void resolve(GameData gameData, StackEntry entry, QueueReflexiveAbilityEffect queueEffect,
                        Consumer<StackEntry> recordSetup) {
        // What the ability records is the same whichever route it takes: the resolving entry's X, event, source and
        // "that player". "That much" and "that many" are the event value, read as such, never the ability's X.
        StackEntry record;
        if (queueEffect.aboutPickedPermanent()) {
            // The permanent the ability's effect acts for ("when a creature is put onto the battlefield this way, it
            // deals damage ...") is the ability's source permanent, as it is now: the next permanent an earlier step
            // of this resolution picked, taken off the entry's picks so the next such step is about the one after.
            List<UUID> picks = entry.getResolutionResults().pickedIds();
            if (picks.isEmpty()) {
                return;
            }
            UUID pickedPermanentId = picks.getFirst();
            entry.recordPicks(picks.subList(1, picks.size()));
            record = ReflexiveAbilitySupport.record(gameData, entry, List.of(queueEffect.effect()),
                    entry.getCard().getName() + "'s reflexive ability", pickedPermanentId);
            Permanent reflexiveSource = gameQueryService.findPermanentById(gameData, pickedPermanentId);
            record.setSourcePermanentSnapshot(reflexiveSource == null ? null : new Permanent(reflexiveSource));
        } else {
            record = ReflexiveAbilitySupport.record(gameData, entry, List.of(queueEffect.effect()));
        }
        if (queueEffect.useEntryTargetAsTriggeringPermanent() && entry.getTargetId() != null) {
            record.setTriggeringPermanentId(entry.getTargetId());
        }
        recordSetup.accept(record);
        ReflexiveAbilitySupport.route(gameData, record, () -> putOnStackOrChoose(gameData, entry, queueEffect, record));
    }

    private void putOnStackOrChoose(GameData gameData, StackEntry entry, QueueReflexiveAbilityEffect queueEffect,
                                    StackEntry record) {
        CardEffect reflexiveEffect = queueEffect.effect();
        if (entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                && reflexiveEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && !reflexiveEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                && !entry.getCard().hasEffectTargetIndex(reflexiveEffect)) {
            // "That player" refers to the player the original trigger carries (the player dealt damage), so a
            // reflexive ability whose player the card declares no target for does not choose a target of its own.
            gameData.stack.add(record);
            return;
        }
        int targetGroupIndex = entry.getCard().getEffectTargetIndex(queueEffect.effect());
        if (isAnyNumberOfTargetsGroup(entry, targetGroupIndex)) {
            // "Any number of target ..." is chosen in one selection (The Thing), when the reflexive
            // ability is put on the stack (CR 603.12, CR 603.3d).
            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                    entry.getCard(), entry.getControllerId(), List.of(queueEffect.effect()),
                    record.getSourcePermanentId(), entry.getControllerId(), entry.getAttackedTargetId(),
                    null, entry.getXValue()));
            triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
            return;
        }
        if (isMultiTargetGroup(entry, targetGroupIndex)) {
            int reflexiveXValue = entry.getXValue();
            List<Integer> precedingGroupSizes = new ArrayList<>();
            for (int i = 0; i < targetGroupIndex; i++) {
                precedingGroupSizes.add(0);
            }
            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(queueEffect.effect()),
                    record.getSourcePermanentId(), List.of(), targetGroupIndex, 0,
                    precedingGroupSizes, reflexiveXValue, List.of(), false,
                    null, null, entry.getEventValue()));
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
            return;
        }
        if (graveyardTargetingSupport.findTarget(List.of(queueEffect.effect())) != null
                || queueEffect.effect().targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            // The choice has the record's X; a card restricted by the event's value is chosen against the record.
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(queueEffect.effect()), null, 0,
                    record.getXValue(), 0, null, false, null, record.getSourcePermanentId()));
            if (!gameData.interaction.isAwaitingInput()) {
                triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
            }
            return;
        }
        if (beginTargetChoice(gameData, entry, record, queueEffect.effect(), queueEffect.optionalTarget())) {
            return;
        }
        gameData.stack.add(record);
    }

    private boolean isAnyNumberOfTargetsGroup(StackEntry entry, int targetGroupIndex) {
        if (targetGroupIndex < 0 || targetGroupIndex >= entry.getCard().getTargetGroups().size()
                || entry.getCard().getTargetGroups().size() != 1) {
            return false;
        }
        var group = entry.getCard().getTargetGroups().get(targetGroupIndex);
        return group.getMaxTargets() == TargetGroup.ANY_NUMBER_OF_TARGETS
                && group.getDynamicMinTargets() == null
                && group.getDynamicMaxTargets() == null;
    }

    private boolean isMultiTargetGroup(StackEntry entry, int targetGroupIndex) {
        if (targetGroupIndex < 0 || targetGroupIndex >= entry.getCard().getTargetGroups().size()) {
            return false;
        }
        var group = entry.getCard().getTargetGroups().get(targetGroupIndex);
        return entry.getCard().getTargetGroups().size() > 1
                || group.getMaxTargets() > 1
                || group.getMinTargets() == 0
                || group.getDynamicMinTargets() != null
                || group.getDynamicMaxTargets() != null;
    }

    private boolean beginTargetChoice(GameData gameData, StackEntry entry, StackEntry record, CardEffect effect,
                                      boolean optionalTarget) {
        TargetSpec targetSpec = effect.targetSpec();
        if (!targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                && !targetSpec.admits(TargetPredicate.Kind.PLAYER)
                && !targetSpec.admits(TargetPredicate.Kind.SPELL)) {
            return false;
        }

        TargetPredicate predicate = targetSpec.targetPredicate();
        int xValue = entry.getXValue();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(record.getSourcePermanentSnapshot())
                .withSourcePermanentId(record.getSourcePermanentId())
                .withDefendingPlayerId(defendingPlayerId(gameData, entry.getAttackedTargetId()))
                .withXValue(xValue)
                .withEventValue(entry.getEventValue())
                .withTriggeringPermanentId(record.getTriggeringPermanentId())
                .withTriggeringPermanentSnapshot(record.getTriggeringPermanentSnapshot())
                .withTriggeringPlayerId(record.getTriggeringPlayerId());
        TargetFilter groupFilter = targetGroupFilter(entry, effect);

        List<UUID> validPermanentIds = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (targetPredicateEvaluationService.matchesPermanent(predicate, permanent, filterContext)
                            && (groupFilter == null || predicateEvaluationService.matchesFilters(
                            permanent, Set.of(groupFilter), filterContext))
                            && targetLegalityService.checkTriggeredPermanentTargetableReason(
                            gameData, permanent, entry.getCard(), entry.getControllerId()).isEmpty()) {
                        validPermanentIds.add(permanent.getId());
                    }
                }
            }
        }

        if (targetSpec.admits(TargetPredicate.Kind.SPELL)) {
            for (StackEntry stackEntry : gameData.stack) {
                if (isSpellStackEntry(stackEntry)
                        && targetPredicateEvaluationService.matchesSpell(
                        predicate, stackEntry, entry.getControllerId(), record.getSourcePermanentSnapshot(),
                        filterContext)) {
                    validPermanentIds.add(stackEntry.getTargetableId());
                }
            }
        }

        List<UUID> validPlayerIds = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (targetPredicateEvaluationService.matchesPlayer(
                        predicate, playerId, entry.getControllerId(), gameData)) {
                    validPlayerIds.add(playerId);
                }
            }
            if (groupFilter != null) {
                validPlayerIds = validTargetService.filterValidPlayerTargets(
                        gameData, groupFilter, validPlayerIds, entry.getControllerId());
            }
        }
        // "Up to one target", as the effect itself declares it: with nothing to target the ability goes on the stack
        // with no target, and with something to target its controller may still choose none.
        boolean upToOneTarget = effect.hasOptionalTarget();
        if (upToOneTarget && validPermanentIds.isEmpty() && validPlayerIds.isEmpty()) {
            gameData.stack.add(record);
            return true;
        }
        boolean mayChooseNone = optionalTarget || upToOneTarget;
        if (mayChooseNone && !validPlayerIds.contains(entry.getControllerId())) {
            validPlayerIds.add(entry.getControllerId());
        }

        if (validPermanentIds.isEmpty() && validPlayerIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s reflexive ability has no valid targets."));
            return true;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(effect), record.getSourcePermanentId(),
                record.getSourcePermanentSnapshot(), entry.getEventValue(), xValue, mayChooseNone,
                entry.getAttackedTargetId()));
        playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), validPermanentIds,
                validPlayerIds, entry.getCard().getName() + "'s reflexive ability - Choose a target.");
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                "'s reflexive ability - choose a target."));
        return true;
    }

    /**
     * The filter of the card target group the reflexive effect is bound to ("when you do, tap
     * target creature an opponent controls"), or {@code null} when the effect's own target spec
     * fully describes its target.
     */
    private TargetFilter targetGroupFilter(StackEntry entry, CardEffect effect) {
        int targetGroupIndex = entry.getCard().getEffectTargetIndex(effect);
        if (targetGroupIndex < 0 || targetGroupIndex >= entry.getCard().getTargetGroups().size()) {
            return null;
        }
        return entry.getCard().getTargetGroups().get(targetGroupIndex).getFilter();
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }

    private boolean isSpellStackEntry(StackEntry entry) {
        return switch (entry.getEntryType()) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }
}
