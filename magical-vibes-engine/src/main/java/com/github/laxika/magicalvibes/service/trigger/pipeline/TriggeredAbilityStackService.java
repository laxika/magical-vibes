package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.EventFactCondition;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.model.condition.EntryFactCondition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.AttackCounterMoveEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.TargetGroup;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.trigger.CreatureDeath;
import com.github.laxika.magicalvibes.service.trigger.CreatureDeathWatch;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.StackedModalTriggerService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.trigger.ZoneChangeWatch;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Puts a triggered ability that the pipeline matched on the stack, for every family moved off {@code EffectSlot}:
 *
 * <ul>
 *   <li>the intervening "if" is checked as it triggers, and wraps its effects so it is checked again as it
 *       resolves (CR 603.4);</li>
 *   <li>a limit stops it from triggering again: once each turn, "do this only once each turn" (CR 603.2h), which
 *       the source's controller uses up by taking the action, and once;</li>
 *   <li>effects that make abilities trigger an additional time (Teysa Karlov's, Panharmonicon-like ones) add
 *       copies.</li>
 * </ul>
 * An ability of the source's own zone change ("when this dies", "leaves the battlefield", "is put into a graveyard
 * from anywhere", "when you sacrifice this") goes on the stack through one default, {@link #triggerOwn}, that records
 * the event whatever its effects are; its options are the only trigger-level properties read (step 14 of
 * effect-slot-refactor.md). It is one stack object (CR 603.3): a modal one waits for its mode (CR 603.3c), one that
 * targets for its targets as it goes on the stack and is removed if none is legal (CR 603.3d), and any other goes on
 * the stack through {@link GameData#enqueueTrigger}.
 * An ability that watches another object die ("whenever a creature you control dies", a relation other than
 * {@code THIS}) goes through {@link #triggerOnce} instead: its options are checked the same way, and its effects go on
 * the stack through its {@link CreatureDeathWatch}'s collector, which records the death whatever they are. So does an
 * ability of
 * one of the other zone-change families step 12a of effect-slot-refactor.md moved, through its
 * {@link ZoneChangeWatch}.
 * APNAP ordering (CR 603.3b) happens later, in {@code TriggeredAbilityQueueService.beginTriggerOrder}.
 */
@Service
public class TriggeredAbilityStackService {

    private final ConditionEvaluationService conditionEvaluationService;
    private final GameQueryService gameQueryService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerTargetCollector triggerTargetCollector;

    public TriggeredAbilityStackService(@Lazy ConditionEvaluationService conditionEvaluationService,
                                        @Lazy GameQueryService gameQueryService,
                                        @Lazy GraveyardTargetingSupport graveyardTargetingSupport,
                                        @Lazy GameLogService gameLogService,
                                        @Lazy TriggerCollectionService triggerCollectionService,
                                        @Lazy AmountEvaluationService amountEvaluationService,
                                        @Lazy PredicateEvaluationService predicateEvaluationService,
                                        @Lazy TriggerTargetCollector triggerTargetCollector) {
        this.predicateEvaluationService = predicateEvaluationService;
        this.triggerTargetCollector = triggerTargetCollector;
        this.triggerCollectionService = triggerCollectionService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameQueryService = gameQueryService;
        this.graveyardTargetingSupport = graveyardTargetingSupport;
        this.gameLogService = gameLogService;
        this.amountEvaluationService = amountEvaluationService;
    }

    /**
     * Which kind of the source's own zone change triggered the ability. The constants are in the order the abilities
     * of one event's moving objects are put on the stack, before their controllers order them (CR 603.3b).
     */
    public enum OwnZoneChange {
        /** Left the battlefield for any zone (CR 603.6c). */
        LEAVES,
        /** Arrived in a zone other than the battlefield: a graveyard from anywhere, a hand, a library, the stack. */
        ARRIVES,
        /** Put into a graveyard from the battlefield (CR 700.4). */
        DIES,
        /** Sacrificed (CR 701.21a). */
        SACRIFICED
    }

    /** The kind of the source's own zone change {@code trigger} is. */
    public static OwnZoneChange ownZoneChangeOf(ZoneChangeTrigger trigger) {
        if (trigger.causes().contains(ZoneChangeCause.SACRIFICE)) {
            return OwnZoneChange.SACRIFICED;
        }
        if (trigger.from().equals(Set.of(Zone.BATTLEFIELD))) {
            return trigger.to() == Zone.GRAVEYARD ? OwnZoneChange.DIES : OwnZoneChange.LEAVES;
        }
        return OwnZoneChange.ARRIVES;
    }

    /**
     * Triggers {@code candidate}'s ability for {@code event}, which met its trigger condition {@code trigger},
     * unless its options or wrappers keep it from triggering.
     */
    public void trigger(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                        ZoneChangeTrigger trigger) {
        if (trigger.relation() != ObjectRelation.THIS
                || ZoneChangeWatch.of(candidate.ability(), trigger, candidate.zone()) != null) {
            triggerOnce(gameData, candidate, List.of(event), trigger);
            return;
        }
        TriggerOptions options = candidate.ability().options();
        Permanent source = candidate.sourceAsPermanent();
        Condition interveningIf = options.interveningIf();
        if (!conditionsMet(gameData, options,
                ConditionContext.forPermanent(source, candidate.controllerId()))) {
            return;
        }
        UUID sourceId = candidate.sourceObjectId();
        String limitKey = limitKey(candidate);
        if (limitReached(gameData, options.limit(), sourceId, limitKey)) {
            return;
        }
        gameData.routeTriggeredAbility(
                () -> triggerOwn(gameData, candidate, event, ownZoneChangeOf(trigger), interveningIf, options));
        markLimit(gameData, options.limit(), sourceId, limitKey);
    }

    /**
     * Triggers {@code candidate}'s "whenever [an object] dies" ability once for {@code events}, the deaths of one
     * batch that met its trigger condition {@code trigger}: once for the one death of an ability that triggers for
     * each, once for all of them for "one or more" (CR 603.2c). Its options are checked as for any ability; its
     * effects go on the stack through its {@link CreatureDeathWatch}'s collector, which records the deaths whatever they
     * are.
     */
    public void triggerOnce(GameData gameData, TriggerCandidate candidate, List<ZoneChangeEvent> events,
                            ZoneChangeTrigger trigger) {
        ZoneChangeWatch zoneChangeWatch = ZoneChangeWatch.of(candidate.ability(), trigger, candidate.zone());
        if (zoneChangeWatch == null && ownZoneChangeOf(trigger) != OwnZoneChange.DIES) {
            return;
        }
        TriggerOptions options = candidate.ability().options();
        Permanent source = candidate.sourceAsPermanent();
        Condition interveningIf = options.interveningIf();
        if (!conditionsMet(gameData, options,
                triggeringContext(gameData, source, candidate.controllerId(), events.getFirst()))) {
            return;
        }
        UUID sourceId = candidate.sourceObjectId();
        String limitKey = limitKey(candidate);
        if (limitReached(gameData, options.limit(), sourceId, limitKey)) {
            return;
        }
        List<CardEffect> effects = new ArrayList<>();
        for (EffectRegistration registration : candidate.ability().effects()) {
            effects.add(withInterveningIf(interveningIf, registration.effect(),
                    trigger.to() == Zone.BATTLEFIELD));
        }
        if (zoneChangeWatch != null) {
            if (triggerCollectionService.triggerZoneChange(gameData, zoneChangeWatch, candidate, effects, events)) {
                markLimit(gameData, options.limit(), sourceId, limitKey);
            }
            return;
        }
        CreatureDeathWatch watch = CreatureDeathWatch.of(trigger, candidate.zone());
        List<CreatureDeath> deaths = events.stream().map(CreatureDeath::of).toList();
        boolean triggered = options.multiplicity() == TriggerMultiplicity.ONE_OR_MORE
                ? triggerCollectionService.triggerCreatureDeaths(gameData, watch, source, candidate.controllerId(),
                        effects, options.limit(), onceEachTurnActionKey(candidate.ability()), deaths)
                : triggerCollectionService.triggerCreatureDeath(gameData, watch, source, candidate.controllerId(),
                        effects, options.limit(), onceEachTurnActionKey(candidate.ability()), deaths.getFirst());
        if (triggered) {
            markLimit(gameData, options.limit(), sourceId, limitKey);
        }
    }

    /**
     * Triggers {@code candidate}'s ability once for {@code events}, the permanents phasing, transforming or being
     * turned face up in one batch that met its trigger condition {@code trigger}: one event for an ability that
     * triggers for each, all of them for "one or more". Its options are checked as for any ability; its effects go
     * on the stack the way the old slot collector of its {@link ZoneChangeWatch} put them there.
     */
    public void triggerPermanentChange(GameData gameData, TriggerCandidate candidate, List<PermanentChangeEvent> events,
                                       PermanentChangeTrigger trigger) {
        ZoneChangeWatch watch = ZoneChangeWatch.of(candidate.ability(), trigger, candidate.zone());
        if (watch == null) {
            return;
        }
        TriggerOptions options = candidate.ability().options();
        Condition interveningIf = options.interveningIf();
        // "If it's a creature" (Trail of Mystery) reads the permanent that changed, as the event's object.
        UUID changedId = events.getFirst().permanent().getId();
        if (!conditionsMet(gameData, options,
                ConditionContext.forPermanent(candidate.sourceAsPermanent(), candidate.controllerId())
                        .withTargetId(changedId).withTriggeringPermanentId(changedId))) {
            return;
        }
        UUID sourceId = candidate.sourceObjectId();
        String limitKey = limitKey(candidate);
        if (limitReached(gameData, options.limit(), sourceId, limitKey)) {
            return;
        }
        List<CardEffect> effects = new ArrayList<>();
        for (EffectRegistration registration : candidate.ability().effects()) {
            effects.add(withInterveningIf(interveningIf, registration.effect(), false));
        }
        if (triggerCollectionService.triggerPermanentChange(gameData, watch, candidate, effects, events)) {
            markLimit(gameData, options.limit(), sourceId, limitKey);
        }
    }

    /**
     * The stack door: puts {@code request}'s ability on the stack, the same way for every ability.
     *
     * <ol>
     *   <li>Its intervening "if" is judged as it triggers, in the request's context, and wraps each effect to be judged
     *       again as it resolves (CR 603.4), but for one about the attack as declared, whose answer is fixed, and for a
     *       modal ability whose mode is chosen as it goes on the stack, which the card writes into each mode. Its limit
     *       ("triggers only once each turn", "only once", "do this only once each turn", CR 603.2h) keeps it from
     *       triggering again and is marked; effects that make it trigger an additional time add stack objects (CR
     *       603.3), unless it triggers only once each turn.</li>
     *   <li>One stack entry is built of its effects as the card wrote them (CR 603.3); a "you may" stays among them and
     *       is asked as it resolves (CR 603.5).</li>
     *   <li>The request's record stamps the event on the entry, whatever the effects are.</li>
     *   <li>A mode chosen as it triggers is announced (CR 603.3c) and its targets are chosen (CR 603.3d): the door
     *       parks the entry with the choice ({@link StackEntry#isParkedAtStackDoor()}), and the choice's handler
     *       finishes it ({@link #finishParked}). An effect with a target chooses it, unless the event names the object
     *       it acts on ("that creature", a non-targeting {@code targetId} other than the source, CR 115.10a) and the
     *       card declares no target for it. An ability with no legal choice is removed.</li>
     *   <li>An ability that chooses nothing goes to {@link GameData#enqueueTrigger}; its controller orders it among the
     *       others later (CR 603.3b).</li>
     * </ol>
     * The door never reads an effect's class: what it needs of an effect it asks through {@code CardEffect}.
     *
     * @return whether the ability triggered
     */
    public boolean put(GameData gameData, TriggeredAbilityRequest request) {
        TriggeredAbility ability = request.ability();
        TriggerOptions options = ability.options();
        Condition interveningIf = options.interveningIf();
        if (interveningIf != null && !conditionEvaluationService.isMet(gameData, interveningIf, request.ifContext())) {
            return false;
        }
        UUID limitSourceId = request.sourcePermanent() != null ? request.sourcePermanentId()
                : request.sourceCard().getId();
        if (limitReached(gameData, options.limit(), limitSourceId, ability.limitKey(), request.limitCreatureId())) {
            return false;
        }
        List<CardEffect> written = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            List<ChooseOneEffect.ChooseOneOption> randomModes = registration.effect().modesChosenAtRandomAsItTriggers();
            if (randomModes == null) {
                written.add(registration.effect());
                continue;
            }
            ChooseOneEffect.ChooseOneOption mode = chooseRandomModeAsItTriggers(gameData, request, randomModes);
            if (mode == null) {
                // No mode can be chosen: the ability is removed from the stack (CR 603.3c).
                return false;
            }
            written.addAll(mode.effectsForSelection());
        }
        List<CardEffect> effects = new ArrayList<>(written.size());
        for (CardEffect effect : written) {
            boolean judgedOnce = interveningIf == null || EventFactCondition.isEventFact(interveningIf)
                    || effect.modesChosenAsItTriggers() != null;
            effects.add(judgedOnce ? effect : new ConditionalEffect(interveningIf, effect));
        }
        int copies = options.limit() == TriggerLimit.ONCE_EACH_TURN || request.sourcePermanent() == null ? 1
                : 1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, request.controllerId(),
                        request.sourcePermanent(), request.attackCaused(), request.event());
        int previousCopies = gameData.beginTriggeredAbilityCopies(copies);
        try {
            gameData.routeTriggeredAbility(() -> route(gameData, request, effects));
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
        markLimit(gameData, options.limit(), limitSourceId, ability.limitKey(), request.limitCreatureId());
        return true;
    }

    /**
     * The mode of a "choose one at random" chosen as {@code request}'s ability is put on the stack (CR 603.3c), at random
     * among {@code modes} that can be chosen: a mode that targets needs a legal target, unless it may have none. {@code
     * null} when none can.
     */
    public ChooseOneEffect.ChooseOneOption chooseRandomModeAsItTriggers(GameData gameData,
                                                                      TriggeredAbilityRequest request,
                                                                      List<ChooseOneEffect.ChooseOneOption> modes) {
        return chooseRandomModeAsItTriggers(gameData, request.controllerId(), request.sourceCard(),
                request.sourcePermanent(), modes);
    }

    /**
     * The mode of a "choose one at random" chosen as an ability of {@code sourceCard} (its permanent
     * {@code sourcePermanent}, or {@code null}) controlled by {@code controllerId} is put on the stack (CR 603.3c), at
     * random among {@code modes} that can be chosen; {@code null} when none can.
     */
    public ChooseOneEffect.ChooseOneOption chooseRandomModeAsItTriggers(GameData gameData, UUID controllerId,
                                                                      Card sourceCard, Permanent sourcePermanent,
                                                                      List<ChooseOneEffect.ChooseOneOption> modes) {
        List<ChooseOneEffect.ChooseOneOption> eligible = new ArrayList<>();
        for (ChooseOneEffect.ChooseOneOption mode : modes) {
            List<CardEffect> modeEffects = mode.effectsForSelection();
            boolean requiresTarget = modeEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().declaredTarget() != null);
            if (!requiresTarget || mode.minTargets() == 0) {
                eligible.add(mode);
                continue;
            }
            TargetFilter targetFilter = mode.targetFilter() != null ? mode.targetFilter() : sourceCard.getTargetFilter();
            if (!triggerTargetCollector.collect(gameData, modeEffects, targetFilter, controllerId, sourceCard,
                    TriggerTargetCollector.Options.END_STEP, sourcePermanent).validTargets().isEmpty()) {
                eligible.add(mode);
            }
        }
        return eligible.isEmpty() ? null : eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
    }

    /**
     * Builds {@code request}'s entry of {@code effects}, stamps its record, and parks it for a choice or queues it. What
     * a choice needs of the event (the attacked player or permanent, the triggering permanent and player, the event's
     * value and X) it reads from the stamped entry.
     */
    private void route(GameData gameData, TriggeredAbilityRequest request, List<CardEffect> effects) {
        Card card = request.sourceCard();
        UUID controllerId = request.controllerId();
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                request.description(), effects, null, request.sourcePermanentId());
        entry.setOnceEachTurnActionKey(onceEachTurnActionKey(request.ability()));
        entry.setGrantingPermanentId(request.ability().grantingPermanentId());
        entry.setTriggeredBySourceAttacking(request.attack() == TriggeredAbilityRequest.AttackCause.SOURCE_ATTACKED);
        if (request.ability().targetFilter() != null) {
            entry.setTargetFilter(request.ability().targetFilter());
        }
        request.record().stamp(entry);
        aimAtTriggeringSpell(gameData, entry, effects);
        gameData.handOffTriggerRecord(entry);
        ChooseOneEffect modes = null;
        for (CardEffect effect : effects) {
            if (effect.modesChosenAsItTriggers() != null) {
                if (effects.size() != 1) {
                    throw new IllegalStateException("A mode chosen as an ability triggers is the ability's one "
                            + "effect: " + card.getName());
                }
                modes = effect.modesChosenAsItTriggers();
            }
        }
        // A "choose one" whose mode can't be announced once the ability is on the stack (a mode that targets a
        // graveyard card, "choose up to", "choose both instead") is announced as the ability goes there (CR 603.3c).
        if (modes == null && effects.size() == 1 && effects.getFirst().modal() != null
                && !StackedModalTriggerService.announcesMode(effects.getFirst().modal())) {
            modes = effects.getFirst().modal();
        }
        List<CardEffect> choosing = effects.stream().filter(effect -> !effect.targetsChosenByReflexiveAbility())
                .toList();
        TargetPredicate.GraveyardCards randomGraveyardTarget = randomGraveyardTarget(choosing);
        if (modes != null) {
            entry.setParkedAtStackDoor(true);
            PermanentChoiceContext.TriggeredModalTrigger modeChoice = new PermanentChoiceContext.TriggeredModalTrigger(
                    card, controllerId, modes, entry.getSourcePermanentId(), false, false, null,
                    entry.getAttackedTargetId(), entry.getTriggeringPermanentId(), false, null)
                    .withTriggerRecord(entry);
            // "Choose one. If [condition], you may choose both instead": the condition is judged as the ability
            // triggers, with its source and controller.
            if (modes.choicesMaxCondition() != null && !conditionEvaluationService.isMet(gameData,
                    modes.choicesMaxCondition(), request.ifContext())) {
                modeChoice = modeChoice.withModeCount(modes.choicesRequired(), modes.choicesRequired());
            }
            // "Choose up to X" (Riku of Many Paths): X is read as the ability goes on the stack, from its record; with
            // no mode to choose, the ability is removed (CR 700.2b).
            DynamicAmount maximum = effects.size() == 1 ? effects.getFirst().maximumModesChosenAsItTriggers() : null;
            if (maximum != null) {
                int maximumModes = Math.min(modes.choicesMax(), Math.max(0, amountEvaluationService.evaluate(
                        gameData, maximum, AmountContext.forStackEntry(entry, request.sourcePermanent()))));
                if (maximumModes == 0) {
                    gameLogService.append(gameData, GameLog.cardThen(card,
                            "'s triggered ability has no modes to choose."));
                    return;
                }
                modeChoice = modeChoice.withModeCount(0, maximumModes);
            }
            gameData.queueInteraction(modeChoice);
        } else if (choosing.stream().anyMatch(AttackCounterMoveEffect.class::isInstance)) {
            // Two targets chosen one after the other, the second "up to one" among the defending player's creatures
            // (Decimator Beetle): the record stays with the choice, whose handler builds the entry.
            gameData.queueInteraction(new PermanentChoiceContext.AttackCounterMoveFirstTarget(
                    card, controllerId, effects, entry.getSourcePermanentId(), entry.getDefendingPlayerId()));
        } else if (randomGraveyardTarget != null) {
            putWithRandomGraveyardTarget(gameData, entry, randomGraveyardTarget);
        } else if (choosing.stream().anyMatch(CardEffect::targetChosenAtRandom)) {
            putWithRandomTarget(gameData, request, entry, choosing);
        } else if (choosing.stream().anyMatch(TriggeredAbilityStackService::choosesGraveyardCard)
                && !targetGroupsOf(card, choosing).isEmpty()
                && (targetGroupsOf(card, choosing).size() > 1 || card.getMultiTargetConstraint() != null)) {
            // A graveyard card and other targets of the card's groups ("up to one other target creature you control …
            // up to one target creature card from your graveyard", Betor, Ancestor's Voice), or graveyard cards under
            // the card's constraint between its targets ("up to one … from each opponent's graveyard", Orzhova):
            // walked group by group.
            entry.setParkedAtStackDoor(true);
            int firstGroup = targetGroupsOf(card, choosing).getFirst().getIndex();
            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                    card, controllerId, effects, entry.getSourcePermanentId(), List.of(), firstGroup, 0,
                    java.util.Collections.nCopies(firstGroup, 0), entry.getXValue())
                    .withEventValue(entry.getEventValue()).withTriggerRecord(entry));
        } else if (choosing.stream().anyMatch(TriggeredAbilityStackService::choosesGraveyardCard)
                && choosing.stream().noneMatch(TriggeredAbilityStackService::choosesBattlefieldOrPlayer)) {
            entry.setParkedAtStackDoor(true);
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    card, controllerId, effects, graveyardOwner(gameData, entry, choosing), 0, entry.getXValue(),
                    graveyardChoiceMaxTargets(entry, choosing), entry.getTriggeringPermanentPowerAtTrigger(),
                    entry.isAlternateCost(), entry.getTriggeringPermanentId(), entry.getSourcePermanentId(),
                    entry.getOnceEachTurnActionKey(), entry.getAttackingPermanentSnapshots(), null, null)
                    .withTriggeringSpell(entry.getTriggeringSpellSnapshot())
                    .withTriggerRecord(entry));
        } else if (choosesTarget(gameData, card, entry, choosing)) {
            entry.setParkedAtStackDoor(true);
            List<TargetGroup> groups = targetGroupsOf(card, choosing);
            // One target that may be a permanent or a graveyard card ("up to one target creature or creature card in a
            // graveyard", Taskmaster, Mercenary Mimic) is chosen in one choice, which offers both.
            boolean mixedGraveyardTarget = groups.size() == 1
                    && !(groups.getFirst().getFilter()
                    instanceof com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter)
                    && choosing.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
            if ((choosesTargetsGroupByGroup(card, groups, choosing)
                    || choosing.stream().anyMatch(CardEffect::dividesAmongTargetsAsItGoesOnTheStack))
                    && !mixedGraveyardTarget) {
                // The groups are walked from the ability's first; earlier groups are another ability's.
                int firstGroup = groups.isEmpty() ? 0 : groups.getFirst().getIndex();
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, controllerId, effects, entry.getSourcePermanentId(), List.of(), firstGroup, 0,
                        java.util.Collections.nCopies(firstGroup, 0), entry.getXValue())
                        .withEventValue(entry.getEventValue()).withTriggerRecord(entry));
            } else {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(card, controllerId, effects,
                        entry.getSourcePermanentId(), targetChooser(gameData, card, entry, choosing),
                        entry.getAttackedTargetId(), entry.getTriggeringPermanentId(), entry.getXValue(),
                        entry.getEventValue(), entry.getTriggeringPlayerId(), entry.getSourceGraveyardEntryVersion())
                        .withTriggerRecord(entry));
            }
        } else {
            gameData.enqueueTrigger(entry);
            notifyAttackingSourceTriggered(gameData, entry);
        }
    }

    /**
     * Aims {@code entry} at "that spell or ability" when one of {@code effects} acts only on a spell or ability on the
     * stack ("counter it unless its controller pays", ward, "copy that spell"): the spell or ability its event was about,
     * its record's triggering spell, which it acts on without targeting it (CR 115.10a), unless the record names
     * another object than a player.
     */
    private static void aimAtTriggeringSpell(GameData gameData, StackEntry entry, List<CardEffect> effects) {
        if (entry.getTriggeringSpellSnapshot() == null
                || entry.getTargetId() != null && !gameData.playerIds.contains(entry.getTargetId())
                || effects.stream().noneMatch(TriggeredAbilityStackService::actsOnlyOnASpell)) {
            return;
        }
        entry.setTargetId(entry.getTriggeringSpellSnapshot().getTargetableId());
        entry.setTargetZone(Zone.STACK);
        entry.setNonTargeting(true);
    }

    /** Whether {@code effect} acts only on a spell or ability on the stack. */
    private static boolean actsOnlyOnASpell(CardEffect effect) {
        TargetSpec spec = effect.targetSpec();
        return spec.admits(TargetPredicate.Kind.SPELL) && !spec.admits(TargetPredicate.Kind.PERMANENT)
                && !spec.admits(TargetPredicate.Kind.PLAYER);
    }

    /**
     * Tells the "a creature you control attacking causes a triggered ability of that creature to trigger" watchers
     * (Firebender Ascension) that {@code entry}, put on the stack, is such an ability of its still attacking source.
     */
    private void notifyAttackingSourceTriggered(GameData gameData, StackEntry entry) {
        if (!entry.isTriggeredBySourceAttacking() || entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null && source.isAttacking()) {
            triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(gameData, source, entry);
        }
    }

    /**
     * Whether {@code effect} may also choose a permanent or player target, beside a graveyard card ("target creature or
     * creature card in a graveyard", Taskmaster, Mercenary Mimic): its target choice offers both.
     */
    private static boolean choosesBattlefieldOrPlayer(CardEffect effect) {
        return effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER);
    }

    /** Whether {@code effect} chooses a graveyard card as its ability goes on the stack (CR 603.3d). */
    private static boolean choosesGraveyardCard(CardEffect effect) {
        return effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD) || effect.graveyardCardChoice() != null;
    }

    /**
     * The graveyard {@code effects} choose from when the event names it: "that player's graveyard" of an ability whose
     * event is about one opponent, its triggering player (an opponent's graveyard, or graveyard cards whose effect names
     * no graveyard of its own), and "defending player's graveyard" of a choice that says so. {@code null} for the
     * graveyards the effects' scope says, "an opponent's graveyard" or "a graveyard" of an attack among them.
     */
    private static UUID graveyardOwner(GameData gameData, StackEntry entry, List<CardEffect> effects) {
        UUID player = null;
        for (CardEffect effect : effects) {
            GraveyardSearchScope scope = effect.targetSpec().graveyardScope().orElse(null);
            GraveyardCardChoosingEffect choice = effect.graveyardCardChoice();
            if (choice != null && choice.choosesFromEventPlayersGraveyard()) {
                player = entry.getDefendingPlayerId() != null ? entry.getDefendingPlayerId()
                        : entry.getTriggeringPlayerId();
                break;
            }
            if ((scope == GraveyardSearchScope.OPPONENT_GRAVEYARD || choice != null && scope == null)
                    && entry.getTriggeringPlayerId() != null) {
                player = entry.getTriggeringPlayerId();
            }
        }
        return player != null && gameData.playerIds.contains(player) && !player.equals(entry.getControllerId())
                ? player : null;
    }

    /**
     * The most cards a graveyard choice of {@code effects} takes when it isn't a declared target (Rysorian Badger's "up
     * to two", Froghemoth's "up to that many", read with the entry's event value), or 0 for the count the effects
     * declare.
     */
    private static int graveyardChoiceMaxTargets(StackEntry entry, List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            GraveyardCardChoosingEffect choice = effect.graveyardCardChoice();
            if (choice != null && !effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                return choice.graveyardChoiceMaxTargets(entry.getEventValue());
            }
        }
        return 0;
    }

    /** The graveyard target of {@code effects} chosen at random ("chosen at random", Power Pack), or {@code null}. */
    private static TargetPredicate.GraveyardCards randomGraveyardTarget(List<CardEffect> effects) {
        return effects.stream()
                .filter(CardEffect::targetChosenAtRandom)
                .map(effect -> effect.targetSpec().declaredTarget())
                .filter(TargetPredicate.GraveyardCards.class::isInstance)
                .map(TargetPredicate.GraveyardCards.class::cast)
                .findFirst().orElse(null);
    }

    /**
     * Puts {@code entry} on the stack with its graveyard target picked at random among the legal ones as it goes there
     * (CR 603.3d); without a legal one the ability is removed.
     */
    private void putWithRandomGraveyardTarget(GameData gameData, StackEntry entry,
                                              TargetPredicate.GraveyardCards target) {
        Card card = entry.getCard();
        List<Card> candidates = new ArrayList<>();
        for (UUID owner : target.scope().graveyardOwners(gameData.orderedPlayerIds, entry.getControllerId())) {
            for (Card graveyardCard : gameData.playerGraveyards.getOrDefault(owner, List.of())) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, target.inner(), card.getId(),
                        gameData, owner)) {
                    candidates.add(graveyardCard);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Card chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        entry.setTargetId(chosen.getId());
        entry.setTargetZone(Zone.GRAVEYARD);
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.cardThen(card,
                "'s ability targets " + chosen.getName() + " at random."));
    }

    /**
     * Puts {@code entry} on the stack with its target, a permanent or player ("target opponent chosen at random",
     * Witch Hunt), picked at random among the legal ones as it goes there (CR 603.3d); without a legal one the ability is
     * removed.
     */
    private void putWithRandomTarget(GameData gameData, TriggeredAbilityRequest request, StackEntry entry,
                                     List<CardEffect> effects) {
        Card card = entry.getCard();
        List<CardEffect> randomlyTargeting = effects.stream().filter(CardEffect::targetChosenAtRandom).toList();
        // The ability's own target filter, else the one the card declares for its effects.
        TargetFilter targetFilter = entry.getTargetFilter() != null ? entry.getTargetFilter()
                : triggerCollectionService.targetFilterForTriggeredEffects(card, randomlyTargeting);
        List<UUID> candidates = triggerTargetCollector.collect(gameData, randomlyTargeting, targetFilter,
                entry.getControllerId(), card, TriggerTargetCollector.Options.END_STEP,
                request.sourcePermanent()).validTargets();
        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s ability has no valid targets."));
            return;
        }
        UUID chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        entry.setTargetId(chosen);
        entry.setNonTargeting(false);
        if (targetFilter != null) {
            entry.setTargetFilter(targetFilter);
        }
        gameData.enqueueTrigger(entry);
        String targetName = gameData.playerIdToName.containsKey(chosen) ? gameData.playerIdToName.get(chosen)
                : java.util.Optional.ofNullable(gameQueryService.findPermanentById(gameData, chosen))
                        .map(permanent -> permanent.getCard().getName()).orElse("a permanent");
        gameLogService.append(gameData, GameLog.cardThen(card, "'s ability randomly targets " + targetName + "."));
    }

    /**
     * Who chooses the target of {@code entry}'s ability: the defending player for a card whose combat ability's target
     * is "of defending player's choice" (the entry's triggering player, else its defending player), the active player
     * for an effect, or a target filter (the ability's own, else the card's), whose target the active player chooses
     * ("target creature of that player's choice"), else its controller.
     */
    private static UUID targetChooser(GameData gameData, Card card, StackEntry entry, List<CardEffect> effects) {
        if (card.isCombatTriggerTargetChosenByDefendingPlayer()) {
            UUID defender = entry.getTriggeringPlayerId() != null ? entry.getTriggeringPlayerId()
                    : entry.getDefendingPlayerId();
            if (defender != null) {
                return defender;
            }
        }
        TargetFilter targetFilter = entry.getTargetFilter() != null ? entry.getTargetFilter() : card.getTargetFilter();
        if ((effects.stream().anyMatch(CardEffect::targetChosenByActivePlayer)
                || targetFilter != null && targetFilter.activePlayerChoosesTarget())
                && gameData.activePlayerId != null) {
            return gameData.activePlayerId;
        }
        return entry.getControllerId();
    }

    /**
     * The card's target groups {@code effects} choose, in order: those bound to them and the bare groups (no effect
     * bound) that belong to them, a bare group belonging to the ability of the next bound group after it ("target
     * attacking creature you control fights up to one target creature you don't control", Gimli's Reckless Might, whose
     * fight reads the first group by index); bound to none, the bare groups. An Aura's first group is what it enchants
     * (CR 303.4a), no ability's target.
     */
    private static List<TargetGroup> targetGroupsOf(Card card, List<CardEffect> effects) {
        List<TargetGroup> bare = card.getTargetGroups().stream()
                .filter(group -> !card.bindsEffectToTargetGroup(group.getIndex()))
                .filter(group -> !(card.isAura() && group.getIndex() == 0))
                .toList();
        List<TargetGroup> groups = card.getTargetGroups().stream()
                .filter(group -> effects.stream().anyMatch(effect ->
                        card.isEffectBoundToTargetGroup(effect, group.getIndex()))
                        || bare.contains(group) && nextBoundGroupIsOf(card, group.getIndex(), effects))
                .toList();
        return groups.stream().anyMatch(group -> card.bindsEffectToTargetGroup(group.getIndex())) ? groups : bare;
    }

    /**
     * Whether the bound group a bare group at {@code groupIndex} belongs to is bound to one of {@code effects}: the first
     * bound group after it, else, with none after it, the last one before it ("have target opponent gain control of
     * target permanent you control", Iroh, Tea Master, whose effect is bound to the player's group). False with none.
     */
    private static boolean nextBoundGroupIsOf(Card card, int groupIndex, List<CardEffect> effects) {
        TargetGroup previous = null;
        for (TargetGroup group : card.getTargetGroups()) {
            if (!card.bindsEffectToTargetGroup(group.getIndex())) {
                continue;
            }
            if (group.getIndex() > groupIndex) {
                return effects.stream().anyMatch(effect -> card.isEffectBoundToTargetGroup(effect, group.getIndex()));
            }
            previous = group;
        }
        // An Aura's bare group is what it enchants (a bestowed creature's), no ability's target.
        TargetGroup owner = previous;
        return owner != null && !card.isAura()
                && effects.stream().anyMatch(effect -> card.isEffectBoundToTargetGroup(effect, owner.getIndex()));
    }

    /**
     * Whether the targets of {@code groups}, an ability's target groups, are chosen group by group: two or
     * more groups, a group with a dynamic count, one "up to one target" group that may target a player, which may be
     * left empty (choosing its controller among players is a target, not a refusal; an "up to one" permanent target is
     * one choice that choosing oneself declines), or an Aura's group after what it enchants, which its effects
     * read by position (Super-Soldier Serum's Equipment). Otherwise the targets of one group of more than
     * one target are announced together in one choice (CR 601.2c), which enforces the card's constraint between them.
     */
    private static boolean choosesTargetsGroupByGroup(Card card, List<TargetGroup> groups, List<CardEffect> effects) {
        boolean mayTargetPlayer = effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                || groups.stream().anyMatch(group -> group.getFilter() instanceof PlayerPredicateTargetFilter
                || group.getFilter() instanceof AnyTargetPredicateTargetFilter);
        return groups.size() > 1 || groups.stream().anyMatch(group -> group.getDynamicMinTargets() != null
                || group.getDynamicMaxTargets() != null
                || group.getMinTargets() == 0 && group.getMaxTargets() == 1 && mayTargetPlayer)
                || card.isAura() && !groups.isEmpty() && groups.getFirst().getIndex() > 0;
    }

    /**
     * Whether {@code entry}'s ability chooses a permanent, player or exiled card target as it goes on the stack
     * (CR 603.3d): an effect admits one, and either the event names no object the effect could act on, or the card
     * declares the effect's target. An object the event names ("that creature", "that player": a non-targeting
     * {@code targetId} other than the ability's source) is what an effect whose target could be that object, and whose
     * target the card doesn't declare, acts on without targeting it (CR 115.10a); "target player or planeswalker" can't
     * be the creature the event names, so it is chosen.
     */
    private boolean choosesTarget(GameData gameData, Card card, StackEntry entry, List<CardEffect> effects) {
        // An effect bound to a target group of this ability alone reads its targets by position, whatever kind it
        // declares ("exchange control of the two target creatures", Glen Elendra).
        List<CardEffect> targeting = effects.stream().filter(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD)
                        || targetsOwnGroup(card, effect, effects)).toList();
        if (targeting.isEmpty()) {
            return false;
        }
        // The source named as the event's object is "that creature" only when it is also the event's permanent (a lone
        // attacking Noble Hierarch's own exalted); otherwise the record names the source as itself.
        boolean eventNamesObject = entry.isNonTargeting() && entry.getTargetId() != null
                && (!entry.getTargetId().equals(entry.getSourcePermanentId())
                || entry.getTargetId().equals(entry.getTriggeringPermanentId()));
        // An ability with a target filter of its own (an emblem's or a granted ability's "target opponent") declares
        // its target.
        if (!eventNamesObject || entry.getTargetFilter() != null) {
            return true;
        }
        return targeting.stream().anyMatch(effect -> declaresTarget(card, effect)
                || !namedObjectFits(gameData, entry.getTargetId(), effect));
    }

    /**
     * Whether {@code card} binds {@code effect} to a target group no other ability of the card has an effect bound to:
     * the group is the ability's own target, which its effect reads by position. A group several abilities of the card
     * share ("target nontoken creature" of Dance of Many's enters, leaves and upkeep abilities) is the target of the one
     * whose effect declares it, and an Aura's first group is what it enchants.
     */
    private static boolean targetsOwnGroup(Card card, CardEffect effect, List<CardEffect> abilityEffects) {
        if (!declaresTarget(card, effect)) {
            return false;
        }
        int group = card.getEffectTargetIndex(effect);
        java.util.stream.Stream<CardEffect> otherEffects = java.util.stream.Stream.concat(
                java.util.stream.Stream.concat(card.getCastingCostsAndSpellEffects().stream(),
                        card.getStaticEffects().stream()),
                card.getTriggeredAbilities().stream()
                        .filter(other -> other.effects().stream().map(EffectRegistration::effect)
                                .noneMatch(abilityEffects::contains))
                        .flatMap(other -> other.effects().stream().map(EffectRegistration::effect)));
        return otherEffects.noneMatch(other -> card.isEffectBoundToTargetGroup(other, group));
    }

    /**
     * Whether {@code card} declares {@code effect}'s target with {@code target(...)}: an effect bound to an Aura's first
     * group acts on what the Aura enchants (CR 303.4a) or its controller, which the ability doesn't target ("enchanted
     * permanent's controller sacrifices …", Strangling Grasp; "deals 2 damage to that player", Insubordination).
     */
    private static boolean declaresTarget(Card card, CardEffect effect) {
        return card.hasEffectTargetIndex(effect) && !(card.isAura() && card.getEffectTargetIndex(effect) == 0);
    }

    /**
     * Whether the object {@code namedId} the event names is what {@code effect} acts on: the effect could target only
     * objects of its kind (a player, or a permanent of its type). An effect that could also target another kind ("any
     * target", "target permanent or player") names a target of its own.
     */
    private boolean namedObjectFits(GameData gameData, UUID namedId, CardEffect effect) {
        TargetSpec spec = effect.targetSpec();
        boolean player = gameData.playerIds.contains(namedId);
        for (TargetPredicate.Kind kind : TargetPredicate.Kind.values()) {
            if (spec.admits(kind) && kind != (player ? TargetPredicate.Kind.PLAYER : TargetPredicate.Kind.PERMANENT)) {
                return false;
            }
        }
        if (player) {
            return spec.admits(TargetPredicate.Kind.PLAYER);
        }
        if (!spec.admits(TargetPredicate.Kind.PERMANENT)) {
            return false;
        }
        Permanent named = gameQueryService.findPermanentById(gameData, namedId);
        PermanentPredicate restriction = effect.targetSpec().targetPredicate().permanentRestriction().orElse(null);
        return named == null || restriction == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, named, restriction);
    }

    /**
     * The door's one continuation: finishes {@code record}, an ability this door parked while its controller made a
     * choice as it went on the stack ({@link StackEntry#isParkedAtStackDoor()}), with {@code choice} (the mode's
     * effects, the targets chosen, or none chosen for an optional target). The ability goes on the stack as a new stack
     * object with everything the record holds; once it has a target, the abilities that trigger on a permanent or player
     * becoming the target of an ability trigger, and choosing an opponent's target is a crime (CR 700.13).
     *
     * @return the stack entry put on the stack
     */
    public StackEntry finishParked(GameData gameData, StackEntry record, Consumer<StackEntry> choice) {
        if (!record.isParkedAtStackDoor()) {
            throw new IllegalStateException("Not parked at the stack door: " + record.getDescription());
        }
        StackEntry entry = record.copyForNewStackObject();
        entry.setParkedAtStackDoor(false);
        choice.accept(entry);
        gameData.enqueueTrigger(entry);
        if (entry.hasAnyTarget() &&!gameData.stack.isEmpty() && gameData.stack.getLast() == entry) {
            triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData);
            triggerCollectionService.checkCrimeTriggers(gameData, entry);
        }
        notifyAttackingSourceTriggered(gameData, entry);
        return entry;
    }

    /** Whether {@code record}, the record a choice was made with, belongs to an ability this door parked. */
    public static boolean parkedAtDoor(StackEntry record) {
        return record != null && record.isParkedAtStackDoor();
    }

    /**
     * Whether the ability's intervening "if" (CR 603.4) and event condition (CR 603.2) let it trigger. Only the
     * intervening "if" is checked again as the ability resolves; the event condition is part of the trigger event.
     */
    private boolean conditionsMet(GameData gameData, TriggerOptions options, ConditionContext context) {
        return (options.interveningIf() == null
                || conditionEvaluationService.isMet(gameData, options.interveningIf(), context))
                && (options.eventCondition() == null
                || conditionEvaluationService.isMet(gameData, options.eventCondition(), context));
    }

    /**
     * Puts {@code candidate}'s ability of its source's own zone change {@code event} on the stack as one object
     * (CR 603.3), recording the event whatever its effects are: the source as it last existed on the battlefield (its
     * snapshot, how it was cast) and as the triggering permanent with what was attached to it ("its counters", "that
     * creature", CR 603.10a), its last-known power (the event value, and at least 0 as the power an amount reads), mana
     * value and controller, the card it left as with the incarnation it has in its new zone ("return it", "if this card
     * is in your graveyard", CR 400.7: the graveyard's, or the exile zone's for "is put into exile from the
     * battlefield"), the card its linked ability exiled (CR 607.2a) and the "do this only once each turn" mark
     * (CR 603.2h). A modal ability waits for its mode (CR 603.3c), one that targets for its targets (CR 603.3d), with
     * the same record; a "you may" is asked as it resolves (CR 603.5). Effects that make an ability trigger an
     * additional time add copies, unless it triggers only once each turn.
     */
    private void triggerOwn(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event, OwnZoneChange kind,
                            Condition interveningIf, TriggerOptions options) {
        List<CardEffect> effects = new ArrayList<>();
        for (EffectRegistration registration : candidate.ability().effects()) {
            effects.add(withInterveningIf(interveningIf, registration.effect(), false));
        }
        Card card = candidate.sourceCard();
        Permanent source = candidate.sourceAsPermanent();
        UUID sourcePermanentId = candidate.sourcePermanent() == null ? null : candidate.sourceObjectId();
        Permanent sourceSnapshot = candidate.sourcePermanent() == null ? null
                : new Permanent(candidate.sourcePermanent());
        UUID controllerId = abilityControllerId(gameData, candidate, effects);
        String markOnAcceptance = onceEachTurnActionKey(candidate.ability());
        Permanent lastKnown = event.permanent();
        int power = lastKnown == null ? 0
                : lastKnown.getLastKnownPower() != null ? lastKnown.getLastKnownPower() : lastKnown.getEffectivePower();
        Card movedCard = event.leftBattlefield() && lastKnown != null && lastKnown.getOriginalCard() != null
                ? lastKnown.getOriginalCard() : card;
        int copies = options.limit() == TriggerLimit.ONCE_EACH_TURN ? 1
                : 1 + additionalTriggers(gameData, candidate, event, kind);
        int previousCopies = gameData.beginTriggeredAbilityCopies(copies);
        try {
            // What the ability records whichever way it reaches the stack: the entry it goes there as when it needs
            // no choice, which a mode choice and the paid graveyard target's choice hand to the entry they build.
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName() + "'s ability", effects, null, sourcePermanentId);
            entry.setNonTargeting(true);
            entry.setOnceEachTurnActionKey(markOnAcceptance);
            if (sourceSnapshot != null) {
                entry.setSourcePermanentSnapshot(sourceSnapshot);
                // How it was cast, for an "if" read after it left: a blitz-only death trigger.
                entry.setAlternateCost(candidate.sourcePermanent().isAlternateCost());
            }
            entry.setEventValue(power);
            entry.setTriggeringPermanentPowerAtTrigger(Math.max(0, power));
            entry.setDyingPermanentManaValue(lastKnown != null ? lastKnown.getCard().getManaValue()
                    : card.getManaValue());
            if (event.to() == Zone.GRAVEYARD) {
                entry.setTriggeringCard(movedCard.getId(), gameData.graveyardEntryVersion(movedCard.getId()));
            }
            if (event.leftBattlefield()) {
                entry.setTriggeringPermanent(lastKnown, event.attachments());
                Card imprinted = gameData.getImprintedCard(card);
                if (imprinted != null) {
                    entry.setLinkedExiledCardId(imprinted.getId());
                }
                if (event.to() == Zone.EXILE && exileOnly(candidate.ability())) {
                    // "When this is put into exile from the battlefield" refers to the exile object it just
                    // became (CR 400.7), so a later exile of the card is another object.
                    entry.setTriggeringCardExileEntryVersion(
                            gameData.exileEntryVersions.getOrDefault(card.getId(), 0L));
                }
            }
            ChooseOneEffect modes = effects.size() == 1 ? modesChosenAsItTriggers(effects.getFirst()) : null;
            gameData.handOffTriggerRecord(entry);
            if (modes != null) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        card, controllerId, modes, sourcePermanentId).withTriggerRecord(entry));
            } else if (targets(effects) && kind == OwnZoneChange.LEAVES) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        card, controllerId, effects, "leaves-the-battlefield", sourcePermanentId, sourceSnapshot
                ).withTriggeringPermanent(lastKnown));
            } else if (paidGraveyardTarget(effects)) {
                // "You may pay {X}. If you do, return target card with mana value X" (Relentless Dead): the card is
                // chosen with the payment's X as the ability goes on the stack.
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        card, controllerId, effects).withTriggerRecord(entry));
            } else if (targets(effects)) {
                PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                        card, controllerId, effects, power, sourceSnapshot, null, null,
                        effects.size() == 1, markOnAcceptance)
                        .withGraveyardTargetMaxManaValue(graveyardTargetMaxManaValue(gameData, controllerId, effects,
                                lastKnown != null ? lastKnown : source));
                if (event.to() == Zone.GRAVEYARD) {
                    target = target.withTriggeringCard(movedCard.getId(),
                            gameData.graveyardEntryVersion(movedCard.getId()));
                }
                gameData.queueInteraction(event.leftBattlefield()
                        ? target.withTriggeringPermanent(lastKnown, event.attachments()) : target);
            } else {
                gameData.enqueueTrigger(entry);
            }
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
        if (kind == OwnZoneChange.LEAVES || kind == OwnZoneChange.ARRIVES) {
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
        }
    }

    /**
     * The modal whose mode the controller chooses as {@code effect}'s ability is put on the stack (CR 603.3c): a
     * trigger-time modal's, or a "choose one" (Ao, the Dawn Sky's "When Ao dies, choose one"); {@code null} for none.
     */
    private static ChooseOneEffect modesChosenAsItTriggers(CardEffect effect) {
        ChooseOneEffect modes = TriggeredModalEffect.choiceMadeAsTriggered(effect);
        return modes != null ? modes : effect instanceof ChooseOneEffect chooseOne ? chooseOne : null;
    }

    /**
     * Whether the ability is a "you may pay" whose paid effect targets only a graveyard card, chosen with the
     * payment's X as it goes on the stack (Relentless Dead's "you may pay {X}. If you do, return another target
     * Zombie creature card with mana value X").
     */
    private static boolean paidGraveyardTarget(List<CardEffect> effects) {
        return effects.size() == 1 && effects.getFirst() instanceof MayPayManaEffect mayPay
                && mayPay.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !mayPay.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                && !mayPay.targetSpec().admits(TargetPredicate.Kind.PLAYER);
    }

    /**
     * The controller of {@code candidate}'s ability: its source's controller, but for the ability Dance of Many gives
     * its token ("when the token leaves the battlefield, sacrifice Dance of Many"), which is the enchantment's and
     * controlled by the enchantment's controller while it is on the battlefield. The engine makes that ability, so it
     * rides on the token (effect-slot-refactor.md step 16).
     */
    private UUID abilityControllerId(GameData gameData, TriggerCandidate candidate, List<CardEffect> effects) {
        if (effects.size() == 1 && effects.getFirst() instanceof RemoveLinkedPermanentEffect remove
                && remove.mode() == RemoveLinkedPermanentEffect.Mode.SACRIFICE_PARTNER
                && candidate.sourcePermanent() != null
                && candidate.sourcePermanent().getChosenPermanentId() != null) {
            UUID partnerControllerId = gameQueryService.findPermanentController(gameData,
                    candidate.sourcePermanent().getChosenPermanentId());
            if (partnerControllerId != null) {
                return partnerControllerId;
            }
        }
        return candidate.controllerId();
    }

    /** Whether every leaves-the-battlefield condition of {@code ability} is a move to exile. */
    private static boolean exileOnly(TriggeredAbility ability) {
        return ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(ZoneChangeTrigger.class::isInstance).map(ZoneChangeTrigger.class::cast)
                .filter(ZoneChangeTrigger::leavesBattlefield)
                .allMatch(trigger -> trigger.to() == Zone.EXILE);
    }

    /**
     * The mana value limit of the graveyard target of {@code effects} ({@code ReturnCardFromGraveyardEffect
     * .dynamicMaxManaValue()}), evaluated as the ability triggers with {@code lastKnown}, the object that moved as it
     * last existed, as the amount's source, so it looks back at the others that died with it (CR 603.10a): Soulshift X
     * counts the Kodama, "with lesser mana value" is the dying object's. The choice carries it to the stack entry.
     * {@code null} when the effects have no such limit.
     */
    private Integer graveyardTargetMaxManaValue(GameData gameData, UUID controllerId, List<CardEffect> effects,
                                                Permanent lastKnown) {
        var target = graveyardTargetingSupport.findTarget(effects);
        if (target == null || target.maximumManaValue() == null) {
            return null;
        }
        return amountEvaluationService.evaluateAtDeath(gameData, target.maximumManaValue(), controllerId, lastKnown);
    }

    /**
     * The context an intervening "if" is checked in as the ability triggers (CR 603.4): its source and controller,
     * and the permanent that moved, for "if that creature's power is greater than this creature's": with its
     * last-known power when it left the battlefield, and its power as the game sees it now when it entered.
     */
    private ConditionContext triggeringContext(GameData gameData, Permanent source, UUID controllerId,
                                               ZoneChangeEvent event) {
        ConditionContext context = ConditionContext.forPermanent(source, controllerId);
        Permanent moved = event.permanent();
        if (moved == null) {
            return context;
        }
        Permanent entered = event.leftBattlefield() ? null : gameQueryService.findPermanentById(gameData, moved.getId());
        int power = entered != null ? gameQueryService.getEffectivePower(gameData, entered)
                : moved.getLastKnownPower() != null ? moved.getLastKnownPower() : moved.getEffectivePower();
        return context.withTriggeringPermanentId(moved.getId())
                .withTriggeringPermanentPowerAtTrigger(Math.max(0, power));
    }

    /** The key the source's limits are kept under for this ability: equal abilities of one source share it. */
    private static String limitKey(TriggerCandidate candidate) {
        return candidate.ability().limitKey();
    }

    /**
     * Whether {@code departed}, a permanent's last-known copy, passes {@code filter} as it last existed on the
     * battlefield (CR 603.10a), as the pipeline judges a departure: for the readers outside it (a face-up plane's
     * "whenever a white creature dies", Agyrem).
     */
    public static boolean departedFilterMatches(PredicateEvaluationService predicates, PermanentPredicate filter,
                                                Permanent departed, DepartedAttachments attachments,
                                                FilterContext context) {
        return TriggerMatcher.departedFilterMatches(predicates, filter, departed, attachments, context);
    }

    /**
     * Whether {@code limit} stops the ability keyed {@code key} of the source {@code sourceId} from triggering again:
     * it triggered already this turn ("triggers only once each turn") or ever ("triggers only once"), or that
     * ability's "do this only once each turn" action was taken this turn (CR 603.2h; {@link #markActionTaken}). Every
     * limit is the ability's own: another limited ability of the same source keeps its own.
     */
    public static boolean limitReached(GameData gameData, TriggerLimit limit, UUID sourceId, String key) {
        return switch (limit) {
            case NONE -> false;
            case ONCE_EACH_TURN -> gameData.keyedOncePerTurnTriggersFiredThisTurn
                    .getOrDefault(sourceId, Set.of()).contains(key);
            // Taking the action (accepting it) marks the ability.
            case DO_ONLY_ONCE_EACH_TURN -> actionTaken(gameData, sourceId, key);
            case ONCE -> gameData.keyedOnceOnlyTriggersFired.getOrDefault(sourceId, Set.of()).contains(key);
            case ONCE_EACH_TURN_FOR_EACH_CREATURE -> throw new IllegalStateException(
                    "Once each turn for each creature is judged only for an event that names the creature");
            // "For the first time each turn" counts the turn's events, which the collectors of those events judge.
            case FIRST_TIME_EACH_TURN -> false;
        };
    }

    /**
     * Marks the ability keyed {@code key} of the source {@code sourceId} as having triggered, for {@code limit}; "do
     * this only once each turn" is marked as the action is taken instead.
     */
    public static void markLimit(GameData gameData, TriggerLimit limit, UUID sourceId, String key) {
        switch (limit) {
            case ONCE_EACH_TURN -> gameData.keyedOncePerTurnTriggersFiredThisTurn
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(key);
            case ONCE -> gameData.keyedOnceOnlyTriggersFired
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(key);
            case NONE, DO_ONLY_ONCE_EACH_TURN, FIRST_TIME_EACH_TURN -> {
            }
        }
    }

    /**
     * The key a stack entry of {@code ability} carries when taking its action uses up the ability's "do this only once
     * each turn" (CR 603.2h): the ability's limit key, or {@code null} for an ability with another limit or none.
     */
    public static String onceEachTurnActionKey(TriggeredAbility ability) {
        return ability.options().limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN ? ability.limitKey() : null;
    }

    /**
     * Whether the "do this only once each turn" action of the ability keyed {@code key} of the source
     * {@code sourceId} was taken this turn (CR 603.2h).
     */
    public static boolean actionTaken(GameData gameData, UUID sourceId, String key) {
        return key != null && sourceId != null
                && gameData.keyedOncePerTurnTriggersFiredThisTurn.getOrDefault(sourceId, Set.of()).contains(key);
    }

    /**
     * Records that the "do this only once each turn" action of the ability keyed {@code key} of the source
     * {@code sourceId} is taken (CR 603.2h): that ability doesn't trigger again this turn. Nothing for a {@code null}
     * key or source.
     */
    public static void markActionTaken(GameData gameData, UUID sourceId, String key) {
        if (key == null || sourceId == null) {
            return;
        }
        gameData.keyedOncePerTurnTriggersFiredThisTurn
                .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(key);
    }

    /** {@link #markActionTaken(GameData, UUID, String)} for {@code entry}'s source and ability. */
    public static void markActionTaken(GameData gameData, StackEntry entry) {
        markActionTaken(gameData, entry.getSourcePermanentId(), entry.getOnceEachTurnActionKey());
    }

    /** {@link #actionTaken(GameData, UUID, String)} for {@code entry}'s source and ability. */
    public static boolean actionTaken(GameData gameData, StackEntry entry) {
        return actionTaken(gameData, entry.getSourcePermanentId(), entry.getOnceEachTurnActionKey());
    }

    /**
     * Whether {@code limit} stops the ability keyed {@code key} of the source {@code sourceId} from triggering again for
     * an event that names the creature {@code creatureId}: "once each turn for each creature" as
     * {@link #markLimit(GameData, TriggerLimit, UUID, String, UUID)} marked the creature for the source, every other
     * limit as {@link #limitReached(GameData, TriggerLimit, UUID, String)} says.
     */
    public static boolean limitReached(GameData gameData, TriggerLimit limit, UUID sourceId, String key,
                                       UUID creatureId) {
        if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE && creatureId != null) {
            return gameData.oncePerCreatureTriggersFiredThisTurn.getOrDefault(sourceId, Set.of()).contains(creatureId);
        }
        return limitReached(gameData, limit, sourceId, key);
    }

    /**
     * Marks the ability keyed {@code key} of the source {@code sourceId} as having triggered, for {@code limit}, for an
     * event that names the creature {@code creatureId}: "once each turn for each creature" marks the creature for the
     * source, every other limit as {@link #markLimit(GameData, TriggerLimit, UUID, String)} says.
     */
    public static void markLimit(GameData gameData, TriggerLimit limit, UUID sourceId, String key, UUID creatureId) {
        if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE && creatureId != null) {
            gameData.oncePerCreatureTriggersFiredThisTurn
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(creatureId);
            return;
        }
        markLimit(gameData, limit, sourceId, key);
    }

    private boolean targets(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                return true;
            }
        }
        return graveyardTargetingSupport.findTarget(effects) != null;
    }

    /**
     * How many additional times effects such as Teysa Karlov's make the ability trigger, as the slot collectors
     * count them. The death of a creature counts the creature-death ones too, except for "when you sacrifice this".
     */
    private int additionalTriggers(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                                   OwnZoneChange kind) {
        Permanent source = candidate.sourceAsPermanent();
        boolean creatureDied = event.died() && event.permanent() != null
                && event.permanent().getCard().hasType(CardType.CREATURE);
        // A creature dying is told apart, so "a creature you control" doublers (Delney, Streetwise Lookout) judge the
        // creature by its last-known controller, and see one that died in the same event (CR 603.10a).
        TriggerContext context = creatureDied ? new TriggerContext.CreatureDeath(event.permanent().getCard(),
                event.controllerId(), 0, 0, event.permanent().getId(), event.permanent()) : null;
        int additional = gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, candidate.controllerId(), source, false, context);
        if (creatureDied && kind != OwnZoneChange.SACRIFICED) {
            additional += gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                    gameData, candidate.controllerId(), source);
        }
        return additional;
    }

    /**
     * {@code effect} with the ability's intervening "if" {@code interveningIf} ({@code null} for none) around it, so it
     * is checked again as the ability resolves (CR 603.4). On an ability triggered by an object entering the
     * battlefield ({@code entering}) it goes inside a "you may" that targets nothing, whose collectors bind the choice to
     * the entering object; the "you may" isn't asked once the "if" is false ({@code EffectResolutionService}). A fact
     * fixed as the object entered ({@link EntryFactCondition}: "if you cast it", "if it was kicked") isn't wrapped: it
     * was judged as the ability triggered, the answer is the same as it resolves, and the stack entry can't always read
     * how the object was cast.
     */
    static CardEffect withInterveningIf(Condition interveningIf, CardEffect effect, boolean entering) {
        if (interveningIf == null || entering && EntryFactCondition.isEntryFact(interveningIf)) {
            return effect;
        }
        if (entering && effect instanceof MayEffect may && TargetSpec.NONE.equals(may.targetSpec())) {
            return new MayEffect(new ConditionalEffect(interveningIf, may.wrapped()), may.prompt(), may.elseEffect(),
                    may.choicePlayer(), may.choicePlayerId());
        }
        return new ConditionalEffect(interveningIf, effect);
    }
}
