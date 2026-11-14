package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.StepEvent;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.StepMatch.StepGroup;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.UUID;

/** Decides whether a rules event meets a candidate ability's trigger condition. No per-slot code. */
final class TriggerMatcher {

    private TriggerMatcher() {
    }

    /**
     * The first of the ability's trigger conditions that {@code event} meets, or {@code null}. A condition is met
     * when its trigger is a zone change of this kind, the moving object relates to the source as the trigger says,
     * and the source is in a zone the condition functions from (CR 113.6).
     *
     * @param movingObject whether the candidate's source is the object that moved, rather than another object that
     *                     sees it move
     * @param predicates   judges the trigger's filter on the moving object
     */
    static ZoneChangeTrigger matchedTrigger(GameData gameData, PredicateEvaluationService predicates,
                                            TriggerCandidate candidate, ZoneChangeEvent event,
                                            boolean movingObject) {
        for (TriggerCondition condition : candidate.ability().conditions()) {
            if (condition.trigger() instanceof ZoneChangeTrigger trigger
                    && condition.zones().functionsIn(candidate.zone())
                    && trigger.matchesMove(event.from(), event.to(), event.cause())
                    && (!trigger.duringYourTurn() || gameData.activePlayerId != null
                    && gameData.activePlayerId.equals(candidate.controllerId()))
                    && causedBy(trigger.causedBy(), candidate, event)
                    && castingSpellMatches(gameData, predicates, trigger, candidate, event)
                    && (movingObject ? relatesToItself(gameData, predicates, trigger, candidate, event)
                    : relates(gameData, predicates, trigger, candidate, event))) {
                return trigger;
            }
        }
        return null;
    }

    /**
     * The first of the ability's trigger conditions that {@code event}, a permanent phasing, transforming or being
     * turned face up, meets, or {@code null}: a permanent-change trigger for that change, functioning from the
     * candidate's zone (CR 113.6), whose permanent relates to the source as it says. The permanent is judged as it is
     * after the change, except one that phased out, which is judged as it last existed (CR 603.10b).
     */
    static PermanentChangeTrigger matchedPermanentChange(GameData gameData, PredicateEvaluationService predicates,
                                                         TriggerCandidate candidate, PermanentChangeEvent event) {
        for (TriggerCondition condition : candidate.ability().conditions()) {
            if (condition.trigger() instanceof PermanentChangeTrigger trigger
                    && condition.zones().functionsIn(candidate.zone())
                    && trigger.changes().contains(event.change())
                    && relatesToChange(gameData, predicates, trigger, candidate, event)) {
                return trigger;
            }
        }
        return null;
    }

    private static boolean relatesToChange(GameData gameData, PredicateEvaluationService predicates,
                                           PermanentChangeTrigger trigger, TriggerCandidate candidate,
                                           PermanentChangeEvent event) {
        Permanent source = candidate.sourcePermanent();
        Permanent changed = event.permanent();
        boolean self = source != null && source.getId().equals(changed.getId());
        boolean controlled = candidate.controllerId().equals(event.controllerId());
        boolean related = switch (trigger.relation()) {
            case THIS -> self;
            case YOU_CONTROL -> controlled;
            case ANY -> true;
            case ANOTHER_YOU_CONTROL -> !self && controlled;
            case ANOTHER -> !self;
            case THIS_OR_ANOTHER_YOU_CONTROL -> self || controlled;
            case THIS_OR_ANOTHER -> true;
            case OPPONENT_CONTROLS -> event.controllerId() != null && !controlled;
            case ENCHANTED, EQUIPPED -> source != null && changed.getId().equals(source.getAttachedTo())
                    && source.getCard().getSubtypes().contains(trigger.relation() == ObjectRelation.EQUIPPED
                    ? CardSubtype.EQUIPMENT : CardSubtype.AURA);
            default -> false;
        };
        if (!related || trigger.filter() == null || self && trigger.relation().thisAlwaysMatches()) {
            return related;
        }
        FilterContext context = event.change() == PermanentChange.PHASED_OUT ? null : FilterContext.of(gameData)
                .withSourceCardId(candidate.sourceCard().getId())
                .withSourceControllerId(candidate.controllerId());
        if (context != null && source != null) {
            // "Another permanent you control is turned face up": the watcher is the filter's source.
            context = context.withSourcePermanentId(source.getId()).withSourcePermanentSnapshot(source);
        }
        return predicates.matchesPermanentPredicate(changed, trigger.filter(), context);
    }

    /**
     * Whether the moving object's own ability sees its own move. "When this dies" does, and "when this dies, if it
     * was a creature" does when its filter matches the object as it last existed. So does an ability whose
     * object may be the source itself, as the object last existed on the battlefield (CR 603.10a): "whenever a
     * creature you control dies" on a creature that dies, or on an enchantment that was animated; "whenever this
     * or another creature dies" whatever it was.
     */
    private static boolean relatesToItself(GameData gameData, PredicateEvaluationService predicates,
                                           ZoneChangeTrigger trigger, TriggerCandidate candidate,
                                           ZoneChangeEvent event) {
        ObjectRelation relation = trigger.relation();
        if (trigger.entersBattlefield()) {
            // The permanent that entered sees its own entry from the battlefield, with the other observers.
            return false;
        }
        if (relation == ObjectRelation.THIS) {
            return trigger.filter() == null || ownFilterMatches(gameData, predicates, trigger.filter(), candidate,
                    event);
        }
        return event.leftBattlefield() && candidate.zone() == Zone.BATTLEFIELD && relation.includesThis()
                && ownsAsTheRelationSays(gameData, relation, candidate, event)
                && (relation.thisAlwaysMatches() || filterMatches(gameData, predicates, trigger, candidate, event));
    }

    /** For the source's own move, whether its owner is the one an owner relation names. */
    private static boolean ownsAsTheRelationSays(GameData gameData, ObjectRelation relation,
                                                 TriggerCandidate candidate, ZoneChangeEvent event) {
        UUID ownerId = ownerOf(gameData, event);
        return switch (relation) {
            case YOU_OWN -> candidate.controllerId().equals(ownerId);
            case OPPONENT_OWNS -> ownerId != null && !candidate.controllerId().equals(ownerId);
            default -> true;
        };
    }

    /** The owner of the object that moved: the event's, or the card's when the event doesn't name one. */
    private static UUID ownerOf(GameData gameData, ZoneChangeEvent event) {
        return event.ownerId() != null ? event.ownerId() : gameData.ownerOf(event.card());
    }

    /** Whether the object that moved relates to another object, the candidate's source, as the trigger says. */
    private static boolean relates(GameData gameData, PredicateEvaluationService predicates,
                                   ZoneChangeTrigger trigger, TriggerCandidate candidate, ZoneChangeEvent event) {
        Permanent source = candidate.sourcePermanent();
        Permanent moved = event.permanent();
        // A permanent that entered is checked for enters abilities too (CR 603.6a), its own entry included.
        boolean self = trigger.entersBattlefield() && source != null && moved != null
                && source.getId().equals(moved.getId());
        if (self) {
            if (!trigger.relation().includesThis()) {
                return false;
            }
            if (trigger.relation().thisAlwaysMatches()) {
                return true;
            }
        } else if (trigger.relation() == ObjectRelation.ANOTHER_YOU_CONTROL || trigger.relation() == ObjectRelation.ANOTHER
                || trigger.relation() == ObjectRelation.ANOTHER_YOU_OWN) {
            if (source != null && moved != null && source.getId().equals(moved.getId())) {
                return false;
            }
        }
        boolean related = switch (trigger.relation()) {
            case THIS -> self;
            case YOU_CONTROL, ANOTHER_YOU_CONTROL, THIS_OR_ANOTHER_YOU_CONTROL ->
                    candidate.controllerId().equals(event.controllerId());
            case ANY, ANOTHER, THIS_OR_ANOTHER -> true;
            case OPPONENT_CONTROLS -> event.controllerId() != null
                    && !candidate.controllerId().equals(event.controllerId());
            case ENCHANTED -> event.attachments().auraCardIds().contains(candidate.sourceCard().getId());
            case EQUIPPED -> source != null && event.attachments().equipmentPermanentIds().contains(source.getId());
            case ENCHANTED_PLAYER_CONTROLS -> source != null && source.getCard().isEnchantPlayer()
                    && event.controllerId() != null && event.controllerId().equals(source.getAttachedTo());
            case HAUNTED -> event.hauntedBy().contains(candidate.sourceCard().getId());
            case YOU_OWN, ANOTHER_YOU_OWN -> candidate.controllerId().equals(ownerOf(gameData, event));
            case OPPONENT_OWNS -> ownerOf(gameData, event) != null
                    && !candidate.controllerId().equals(ownerOf(gameData, event));
            case DAMAGED_BY_THIS -> source != null && moved != null
                    && moved.getPermanentIdsThatDealtDamageThisTurn().contains(source.getId());
            case DAMAGED_BY_ATTACHED -> source != null && moved != null && source.getAttachedTo() != null
                    && moved.getPermanentIdsThatDealtDamageThisTurn().contains(source.getAttachedTo());
        };
        return related && filterMatches(gameData, predicates, trigger, candidate, event);
    }

    /**
     * Whether the resolving spell or ability that moved the object is controlled by the player the trigger names:
     * "a spell or ability an opponent controls causes …". A move nothing resolving caused matches only {@code ANY}.
     */
    private static boolean causedBy(PlayerRelation causedBy, TriggerCandidate candidate, ZoneChangeEvent event) {
        return switch (causedBy) {
            case ANY -> true;
            case YOU -> candidate.controllerId().equals(event.causeControllerId());
            case OPPONENT -> event.causeControllerId() != null
                    && !candidate.controllerId().equals(event.causeControllerId());
        };
    }

    /**
     * Whether the spell the moving object's controller was casting as it moved is the one the trigger asks for: "when
     * you sacrifice this while casting a spell with emerge" (CR 601.2), whether or not that spell's emerge cost is the
     * one being paid (Foul Emissary's ruling). Only the controller of a permanent can sacrifice it (CR 701.21a), so the
     * caster is the candidate's controller.
     */
    private static boolean castingSpellMatches(GameData gameData, PredicateEvaluationService predicates,
                                               ZoneChangeTrigger trigger, TriggerCandidate candidate,
                                               ZoneChangeEvent event) {
        if (trigger.castingSpell() == null) {
            return true;
        }
        return event.castingSpell() != null && candidate.controllerId().equals(event.controllerId())
                && predicates.matchesCardPredicate(event.castingSpell(), trigger.castingSpell(),
                candidate.sourceCard().getId(), gameData, candidate.controllerId());
    }

    /**
     * Whether the moving object passes the trigger's filter. A departed object is judged by its last-known copy,
     * whose card holds the types, colors and keywords it left with (CR 603.10a), and a card that moved between
     * other zones by its card. A permanent that left the battlefield is judged as {@link #departedMatches} says, one
     * that entered as {@link #enteredMatches} says.
     */
    private static boolean filterMatches(GameData gameData, PredicateEvaluationService predicates,
                                         ZoneChangeTrigger trigger, TriggerCandidate candidate,
                                         ZoneChangeEvent event) {
        if (trigger.filter() == null) {
            return true;
        }
        if (event.permanent() == null) {
            return predicates.matchesPermanentPredicate(new Permanent(event.card()), trigger.filter(), null);
        }
        Permanent watcher = candidate.sourceAsPermanent();
        FilterContext context = FilterContext.of(gameData)
                .withSourceControllerId(candidate.controllerId())
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourcePermanentId(watcher.getId())
                .withSourcePermanentSnapshot(watcher)
                .withTriggeringPermanentId(event.permanent().getId())
                .withTriggeringPermanentControllerId(event.controllerId());
        if (!event.leftBattlefield()) {
            Permanent live = onBattlefield(gameData, event.permanent().getId());
            return enteredMatches(predicates, trigger.filter(), event.permanent(),
                    live != null ? live : event.permanent(), context);
        }
        return departedMatches(predicates, trigger.filter(), event.permanent(), event.attachments(), context);
    }

    private static Permanent onBattlefield(GameData gameData, UUID permanentId) {
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(permanentId)) {
                    return permanent;
                }
            }
        }
        return null;
    }

    /**
     * Whether a permanent that entered matches {@code predicate} as the game checks it after the event (CR 603.6a):
     * "whenever another Elf you control enters", "a creature with power 4 or greater", "a creature you didn't cast".
     * Its characteristics are those of {@code asEntered}, its copy with the characteristics it has as its enters
     * abilities are processed. Anything else reads {@code live}, the permanent on the battlefield (its copy once it
     * has left), with the watching ability's source as the filter's source and the entered permanent as the
     * triggering one: its layered power, its counters, where it came from, whether it is the source.
     */
    private static boolean enteredMatches(PredicateEvaluationService predicates, PermanentPredicate predicate,
                                          Permanent asEntered, Permanent live, FilterContext context) {
        return switch (predicate) {
            case PermanentAllOfPredicate allOf -> allOf.predicates().stream()
                    .allMatch(part -> enteredMatches(predicates, part, asEntered, live, context));
            case PermanentAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(part -> enteredMatches(predicates, part, asEntered, live, context));
            case PermanentNotPredicate not -> !enteredMatches(predicates, not.predicate(), asEntered, live, context);
            default -> isCharacteristic(predicate)
                    ? predicates.matchesPermanentPredicate(asEntered, predicate, null)
                    : predicates.matchesPermanentPredicate(live, predicate, context);
        };
    }

    /**
     * Whether {@code departed}, a permanent's last-known copy, passes {@code filter} as it last existed on the
     * battlefield (CR 603.10a), for a reader outside the pipeline (a face-up plane's "whenever a white creature dies").
     */
    static boolean departedFilterMatches(PredicateEvaluationService predicates, PermanentPredicate filter,
                                                Permanent departed, DepartedAttachments attachments,
                                                FilterContext context) {
        return departedMatches(predicates, filter, departed, attachments, context);
    }

    /**
     * Whether a permanent that left the battlefield, seen by another object's ability, matches {@code predicate} as
     * it last existed (CR 603.10a): "whenever another Zombie you control dies", "a creature with power 5 or greater",
     * "a creature with a -1/-1 counter on it", "a creature blocking this creature". Its subtypes and colors are the
     * last-known ones the departure recorded, its other characteristics the ones its last-known card left with, its
     * power and toughness the last-known ones, its Auras and Equipment those attached as it left. Anything else reads
     * the game with the watching ability's source as the filter's source and the departed permanent as the
     * triggering one.
     */
    private static boolean departedMatches(PredicateEvaluationService predicates, PermanentPredicate predicate,
                                           Permanent departed, DepartedAttachments attachments,
                                           FilterContext context) {
        return switch (predicate) {
            case PermanentAllOfPredicate allOf -> allOf.predicates().stream()
                    .allMatch(part -> departedMatches(predicates, part, departed, attachments, context));
            case PermanentAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(part -> departedMatches(predicates, part, departed, attachments, context));
            case PermanentNotPredicate not -> !departedMatches(predicates, not.predicate(), departed, attachments,
                    context);
            case PermanentIsEnchantedPredicate ignored -> !attachments.auraCardIds().isEmpty()
                    || predicates.matchesPermanentPredicate(departed, predicate, context);
            case PermanentIsEquippedPredicate ignored -> !attachments.equipmentPermanentIds().isEmpty()
                    || predicates.matchesPermanentPredicate(departed, predicate, context);
            case PermanentHasSubtypePredicate subtype when departed.getLastKnownSubtypes() != null ->
                    departed.getLastKnownSubtypes().contains(subtype.subtype());
            case PermanentHasAnySubtypePredicate any when departed.getLastKnownSubtypes() != null ->
                    any.subtypes().stream().anyMatch(departed.getLastKnownSubtypes()::contains);
            case PermanentColorInPredicate color when departed.getLastKnownColors() != null ->
                    color.colors().stream().anyMatch(departed.getLastKnownColors()::contains);
            case PermanentIsColorlessPredicate ignored when departed.getLastKnownColors() != null ->
                    departed.getLastKnownColors().isEmpty();
            case PermanentIsMonocoloredPredicate ignored when departed.getLastKnownColors() != null ->
                    departed.getLastKnownColors().size() == 1;
            case PermanentIsMulticoloredPredicate ignored when departed.getLastKnownColors() != null ->
                    departed.getLastKnownColors().size() > 1;
            default -> {
                if (isCharacteristic(predicate)) {
                    yield predicates.matchesPermanentPredicate(departed, predicate, null);
                }
                Boolean stats = departed.getLastKnownToughness() == null ? null
                        : TriggerCollectionService.matchesDyingStats(predicate, departed.getLastKnownPower() != null
                        ? departed.getLastKnownPower() : departed.getEffectivePower(),
                        departed.getLastKnownToughness());
                yield stats != null ? stats : predicates.matchesPermanentPredicate(departed, predicate, context);
            }
        };
    }

    /**
     * Whether {@code predicate} asks about a characteristic the departed copy's card holds (types, subtypes,
     * supertypes, colors, keywords, being a token or face down), which a layered query would read from the game as
     * it is after the departure.
     */
    private static boolean isCharacteristic(PermanentPredicate predicate) {
        return predicate instanceof PermanentIsCreaturePredicate
                || predicate instanceof PermanentIsArtifactPredicate
                || predicate instanceof PermanentIsLandPredicate
                || predicate instanceof PermanentIsEnchantmentPredicate
                || predicate instanceof PermanentIsPlaneswalkerPredicate
                || predicate instanceof PermanentIsBattlePredicate
                || predicate instanceof PermanentHasCardTypePredicate
                || predicate instanceof PermanentIsTokenPredicate
                || predicate instanceof PermanentIsFaceDownPredicate
                || predicate instanceof PermanentIsHistoricPredicate
                || predicate instanceof PermanentHasSubtypePredicate
                || predicate instanceof PermanentHasAnySubtypePredicate
                || predicate instanceof PermanentHasSupertypePredicate
                || predicate instanceof PermanentColorInPredicate
                || predicate instanceof PermanentIsColorlessPredicate
                || predicate instanceof PermanentIsMonocoloredPredicate
                || predicate instanceof PermanentIsMulticoloredPredicate
                || predicate instanceof PermanentHasKeywordPredicate;
    }

    /**
     * Whether the source's own move passes the filter of its "when this …" trigger: "when this creature dies, if it
     * had a -1/-1 counter on it", "if it was a creature", "if it was enchanted". Such a condition is about the object
     * as it last existed (CR 603.10a), which can't change before the ability resolves, so it is a filter rather than
     * an intervening "if" checked again on resolution. Its card types are the ones its last-known card left with, its
     * power and toughness the last-known ones, and its Auras and Equipment the ones attached as it left, those that
     * left in the same event included. Anything else reads the game, with the source as its own source: who owns it,
     * whether it dealt combat damage this turn.
     */
    private static boolean ownFilterMatches(GameData gameData, PredicateEvaluationService predicates,
                                            PermanentPredicate filter, TriggerCandidate candidate,
                                            ZoneChangeEvent event) {
        Permanent moved = event.permanent() != null ? event.permanent() : new Permanent(event.card());
        FilterContext context = FilterContext.of(gameData)
                .withSourceControllerId(candidate.controllerId())
                .withSourceCardId(moved.getOriginalCard().getId())
                .withSourcePermanentSnapshot(moved)
                .withSourcePermanentId(moved.getId());
        return lastKnownMatches(predicates, filter, moved, event.permanent() == null ? null : event.attachments(),
                context);
    }

    private static boolean lastKnownMatches(PredicateEvaluationService predicates, PermanentPredicate predicate,
                                            Permanent moved, DepartedAttachments attachments,
                                            FilterContext context) {
        return switch (predicate) {
            case PermanentAllOfPredicate allOf -> allOf.predicates().stream()
                    .allMatch(part -> lastKnownMatches(predicates, part, moved, attachments, context));
            case PermanentAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(part -> lastKnownMatches(predicates, part, moved, attachments, context));
            case PermanentNotPredicate not -> !lastKnownMatches(predicates, not.predicate(), moved, attachments,
                    context);
            case PermanentIsCreaturePredicate ignored -> moved.getCard().hasType(CardType.CREATURE);
            case PermanentIsLandPredicate ignored -> moved.getCard().hasType(CardType.LAND);
            case PermanentIsEnchantedPredicate ignored when attachments != null ->
                    !attachments.auraCardIds().isEmpty();
            case PermanentIsEquippedPredicate ignored when attachments != null ->
                    !attachments.equipmentPermanentIds().isEmpty();
            default -> {
                Boolean stats = moved.getLastKnownToughness() == null ? null
                        : TriggerCollectionService.matchesDyingStats(predicate, moved.getLastKnownPower() != null
                        ? moved.getLastKnownPower() : moved.getEffectivePower(), moved.getLastKnownToughness());
                yield stats != null ? stats : predicates.matchesPermanentPredicate(moved, predicate, context);
            }
        };
    }

    /**
     * Whether {@code event} meets the trigger {@code effect}, a registration in {@code slot}, will have once its
     * family moves ({@link LegacySlotTriggers}), functioning from that trigger's default zones.
     */
    static boolean matchesAsMoved(EffectSlot slot, CardEffect effect, TriggerCandidate candidate,
                                  ZoneChangeEvent event) {
        ZoneChangeTrigger trigger = LegacySlotTriggers.of(slot, effect);
        return trigger != null && matches(trigger, trigger.defaultZones().functionsIn(candidate.zone()), event);
    }

    /**
     * Whether a static ability such as Hushbringer's keeps {@code event}, a creature dying, from triggering the
     * ability. It doesn't keep "when you sacrifice this" from triggering.
     */
    static boolean suppressed(TriggeredAbility ability, ZoneChangeEvent event) {
        if (!event.died() || !event.deathTriggersSuppressed()) {
            return false;
        }
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof ZoneChangeTrigger trigger
                    && !trigger.causes().contains(ZoneChangeCause.SACRIFICE)) {
                return true;
            }
        }
        return false;
    }

    /** {@link #suppressed} for a slot registration, with the trigger it will have once its family moves. */
    static boolean suppressedAsMoved(EffectSlot slot, CardEffect effect, ZoneChangeEvent event) {
        ZoneChangeTrigger trigger = LegacySlotTriggers.of(slot, effect);
        return event.died() && event.deathTriggersSuppressed() && trigger != null
                && !trigger.causes().contains(ZoneChangeCause.SACRIFICE);
    }

    /** The slot of a candidate whose ability is still held in one, or {@code null}. */
    static EffectSlot legacySlot(TriggerCandidate candidate) {
        return candidate.ability().conditions().getFirst().trigger() instanceof LegacySlotTrigger legacy
                ? legacy.slot() : null;
    }

    private static boolean matches(ZoneChangeTrigger trigger, boolean functions, ZoneChangeEvent event) {
        return functions && trigger.relation() == ObjectRelation.THIS
                && trigger.matchesMove(event.from(), event.to(), event.cause());
    }
    /**
     * How {@code event}, a step or phase beginning, meets one of the candidate's step trigger conditions, or
     * {@code null} when it meets none: the player whose step it is relates to the ability's controller (or to what
     * the source enchants) as the condition says, and the source is where the condition functions (CR 113.6). An
     * ability triggers once for the step however many of its conditions it meets (CR 603.2c).
     *
     * <p>A Curse's "each" step ability acts on the enchanted player, and an Aura's "your upkeep" ability whose effect
     * names a player for the permanent it enchants ("enchanted permanent's controller sacrifices …", Strangling
     * Grasp) acts on that permanent's controller. An emblem's step ability names "your", "each opponent's" or "each"
     * step; another relation fails loudly.
     */
    static StepMatch matchedStep(GameData gameData, GameQueryService query, StepCandidate step, StepEvent event) {
        TriggerCandidate candidate = step.candidate();
        UUID activeId = event.activePlayerId();
        UUID controllerId = candidate.controllerId();
        for (TriggerCondition condition : candidate.ability().conditions()) {
            if (!(condition.trigger() instanceof StepTrigger trigger) || trigger.moment() != event.moment()) {
                continue;
            }
            StepMatch match;
            if (step.counterAbility() != null) {
                match = new StepMatch(step, StepGroup.EXILE_COUNTER, activeId, null);
            } else if (step.plane() != null) {
                match = trigger.player() == ActivePlayer.YOU
                        || trigger.player() == ActivePlayer.ANY
                        ? new StepMatch(step, StepGroup.PLANE, activeId, null) : null;
            } else if (step.emblem() != null) {
                match = switch (trigger.player()) {
                    case YOU -> controllerId.equals(activeId) ? new StepMatch(step, StepGroup.EMBLEM, activeId, null)
                            : null;
                    case OPPONENT -> !controllerId.equals(activeId)
                            ? new StepMatch(step, StepGroup.EMBLEM, activeId, null) : null;
                    case ANY -> new StepMatch(step, StepGroup.EMBLEM, activeId, null);
                    default -> throw new IllegalStateException(candidate.sourceCard().getName()
                            + "'s emblem has a step ability no step reads: " + candidate.ability());
                };
            } else if (!condition.zones().functionsIn(candidate.zone())) {
                match = null;
            } else if (candidate.zone() == Zone.BATTLEFIELD) {
                match = battlefieldStepMatch(gameData, query, step, trigger, activeId);
            } else {
                match = cardStepMatch(gameData, step, trigger, activeId);
            }
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private static StepMatch battlefieldStepMatch(GameData gameData, GameQueryService query, StepCandidate step,
                                                  StepTrigger trigger, UUID activeId) {
        TriggerCandidate candidate = step.candidate();
        UUID controllerId = candidate.controllerId();
        Permanent source = candidate.sourcePermanent();
        UUID attachedTo = source.isAttached() ? source.getAttachedTo() : null;
        boolean enchantsPlayer = attachedTo != null && gameData.playerIds.contains(attachedTo);
        UUID enchantedControllerId = attachedTo != null && !enchantsPlayer
                ? query.findPermanentController(gameData, attachedTo) : null;
        return switch (trigger.player()) {
            case YOU -> {
                if (!controllerId.equals(activeId)) {
                    yield null;
                }
                if (enchantedControllerId != null && actsOnEnchantedPermanentController(candidate)) {
                    yield new StepMatch(step, StepGroup.ENCHANTED_CONTROLLER, enchantedControllerId, attachedTo);
                }
                yield new StepMatch(step, StepGroup.YOU_BATTLEFIELD, activeId, null);
            }
            case ANY -> enchantsPlayer ? new StepMatch(step, StepGroup.ENCHANTED_PLAYER, attachedTo, null)
                    : new StepMatch(step, StepGroup.EACH_BATTLEFIELD, activeId, null);
            case OPPONENT -> controllerId.equals(activeId) ? null
                    : new StepMatch(step, StepGroup.OPPONENT_BATTLEFIELD, activeId, null);
            case CHOSEN_PLAYER -> {
                UUID chosen = source.getChosenPlayerIds().isEmpty() ? source.getRememberedTargetPlayerId()
                        : source.getChosenPlayerIds().getFirst();
                yield activeId.equals(chosen) ? new StepMatch(step, StepGroup.OPPONENT_BATTLEFIELD, activeId, null)
                        : null;
            }
            case ENCHANTED_PLAYER -> enchantsPlayer && attachedTo.equals(activeId)
                    ? new StepMatch(step, StepGroup.ENCHANTED_PLAYER, activeId, null) : null;
            case ENCHANTED_PERMANENT_CONTROLLER -> activeId.equals(enchantedControllerId)
                    ? new StepMatch(step, StepGroup.ENCHANTED_CONTROLLER, activeId, attachedTo) : null;
        };
    }

    private static StepMatch cardStepMatch(GameData gameData, StepCandidate step, StepTrigger trigger,
                                           UUID activeId) {
        TriggerCandidate candidate = step.candidate();
        boolean yours = candidate.controllerId().equals(activeId);
        StepGroup you;
        StepGroup each;
        switch (candidate.zone()) {
            case COMMAND -> {
                you = StepGroup.YOU_COMMAND;
                each = StepGroup.EACH_COMMAND;
            }
            case GRAVEYARD -> {
                you = StepGroup.YOU_GRAVEYARD;
                each = StepGroup.EACH_GRAVEYARD;
            }
            case EXILE -> {
                you = StepGroup.YOU_EXILE;
                each = StepGroup.EACH_EXILE;
            }
            default -> {
                return null;
            }
        }
        return switch (trigger.player()) {
            case YOU -> yours ? new StepMatch(step, you, activeId, null) : null;
            case ANY -> new StepMatch(step, each, activeId, null);
            case OPPONENT -> yours ? null : new StepMatch(step, each, activeId, null);
            default -> null;
        };
    }

    /**
     * Whether the candidate, an ability of an Aura enchanting a permanent, acts on the controller of that permanent:
     * an effect bound to the Aura's enchant target that names a player ("At the beginning of your upkeep, enchanted
     * permanent's controller sacrifices …", Strangling Grasp). The player it names is that permanent's controller,
     * not a target.
     */
    private static boolean actsOnEnchantedPermanentController(TriggerCandidate candidate) {
        return candidate.sourceCard().isAura() && candidate.ability().effects().stream()
                .map(EffectRegistration::effect)
                .anyMatch(effect -> candidate.sourceCard().isEffectBoundToTargetGroup(effect, 0)
                        && effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
    }
    /**
     * The trigger condition of {@code ability} on the combat event {@code watched} names ({@link
     * CombatTrigger#watchKey()}: the event and how the creature relates to the source, its filter aside) that functions
     * in {@code zone}, with its filter, or {@code null} when it has none. A {@code null} zone is the zone of an object
     * whose abilities function wherever it is: a planar object, an emblem, a delayed ability.
     */
    static CombatTrigger combatCondition(TriggeredAbility ability, CombatTrigger watched, Zone zone) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof CombatTrigger combat && combat.watchKey().equals(watched)
                    && (zone == null || condition.zones().functionsIn(zone))) {
                return combat;
            }
        }
        return null;
    }
}
