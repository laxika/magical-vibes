package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
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
import com.github.laxika.magicalvibes.model.condition.EntryFactCondition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
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
import com.github.laxika.magicalvibes.service.trigger.ZoneChangeWatch;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    public TriggeredAbilityStackService(@Lazy ConditionEvaluationService conditionEvaluationService,
                                        @Lazy GameQueryService gameQueryService,
                                        @Lazy GraveyardTargetingSupport graveyardTargetingSupport,
                                        @Lazy GameLogService gameLogService,
                                        @Lazy TriggerCollectionService triggerCollectionService,
                                        @Lazy AmountEvaluationService amountEvaluationService) {
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
        triggerOwn(gameData, candidate, event, ownZoneChangeOf(trigger), interveningIf, options);
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
                        effects, options.limit(), deaths)
                : triggerCollectionService.triggerCreatureDeath(gameData, watch, source, candidate.controllerId(),
                        effects, options.limit(), deaths.getFirst());
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
        boolean markOnAcceptance = options.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        Permanent lastKnown = event.permanent();
        int power = lastKnown == null ? 0
                : lastKnown.getLastKnownPower() != null ? lastKnown.getLastKnownPower() : lastKnown.getEffectivePower();
        Card movedCard = event.leftBattlefield() && lastKnown != null && lastKnown.getOriginalCard() != null
                ? lastKnown.getOriginalCard() : card;
        int copies = options.limit() == TriggerLimit.ONCE_EACH_TURN ? 1
                : 1 + additionalTriggers(gameData, candidate, event, kind);
        int previousCopies = gameData.beginTriggeredAbilityCopies(copies);
        try {
            ChooseOneEffect modes = effects.size() == 1 ? modesChosenAsItTriggers(effects.getFirst()) : null;
            if (modes != null) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        card, controllerId, modes, sourcePermanentId));
            } else if (targets(effects) && kind == OwnZoneChange.LEAVES) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        card, controllerId, effects, "leaves-the-battlefield", sourcePermanentId, sourceSnapshot
                ).withTriggeringPermanent(lastKnown));
            } else if (paidGraveyardTarget(effects)) {
                // "You may pay {X}. If you do, return target card with mana value X" (Relentless Dead): the card is
                // chosen with the payment's X as the ability goes on the stack.
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        card, controllerId, effects));
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
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                        card.getName() + "'s ability", effects, null, sourcePermanentId);
                entry.setNonTargeting(true);
                entry.setMarkSourceOncePerTurnOnAcceptance(markOnAcceptance);
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
     * it triggered already this turn ("triggers only once each turn") or ever ("triggers only once"), or the source's
     * "do this only once each turn" action was taken this turn (CR 603.2h).
     */
    public static boolean limitReached(GameData gameData, TriggerLimit limit, UUID sourceId, String key) {
        return switch (limit) {
            case NONE -> false;
            case ONCE_EACH_TURN -> gameData.keyedOncePerTurnTriggersFiredThisTurn
                    .getOrDefault(sourceId, Set.of()).contains(key);
            // Taking the action (accepting it) marks the source.
            case DO_ONLY_ONCE_EACH_TURN -> gameData.oncePerTurnTriggersFiredThisTurn.contains(sourceId);
            case ONCE -> gameData.keyedOnceOnlyTriggersFired.getOrDefault(sourceId, Set.of()).contains(key);
            case ONCE_EACH_TURN_FOR_EACH_CREATURE -> throw new IllegalStateException(
                    "Once each turn for each creature is read only by the damage and counter collectors");
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
     * Whether {@code limit} stops the ability keyed {@code key} of the source {@code sourceId} from triggering again for
     * an event that names the creature {@code creatureId}: "once each turn for each creature" as
     * {@link #markLimit(GameData, TriggerLimit, UUID, String, UUID)} marked the creature for the source, every other
     * limit as {@link #limitReached(GameData, TriggerLimit, UUID, String)} says.
     */
    public static boolean limitReached(GameData gameData, TriggerLimit limit, UUID sourceId, String key,
                                       UUID creatureId) {
        if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
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
        if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
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
