package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

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
                    && (!trigger.duringYourTurn() || candidate.controllerId().equals(gameData.activePlayerId))
                    && causedBy(trigger.causedBy(), candidate, event)
                    && (movingObject ? relatesToItself(gameData, predicates, trigger, candidate, event)
                    : relates(gameData, predicates, trigger, candidate, event))) {
                return trigger;
            }
        }
        return null;
    }

    /**
     * Whether the moving object's own ability sees its own move. "When this dies" does. So does an ability whose
     * object may be the source itself, as the object last existed on the battlefield (CR 603.10a): "whenever a
     * creature you control dies" on a creature that dies, or on an enchantment that was animated; "whenever this
     * or another creature dies" whatever it was.
     */
    private static boolean relatesToItself(GameData gameData, PredicateEvaluationService predicates,
                                           ZoneChangeTrigger trigger, TriggerCandidate candidate,
                                           ZoneChangeEvent event) {
        ObjectRelation relation = trigger.relation();
        if (relation == ObjectRelation.THIS) {
            return true;
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
        boolean related = switch (trigger.relation()) {
            case THIS -> false;
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
     * Whether the moving object passes the trigger's filter. A departed object is judged by its last-known copy,
     * whose card holds the types, colors and keywords it left with (CR 603.10a), so no layered query runs on it:
     * one would read the game as it is after the departure.
     */
    private static boolean filterMatches(GameData gameData, PredicateEvaluationService predicates,
                                         ZoneChangeTrigger trigger, TriggerCandidate candidate,
                                         ZoneChangeEvent event) {
        if (trigger.filter() == null) {
            return true;
        }
        Permanent moved = event.permanent() != null ? event.permanent() : new Permanent(event.card());
        return predicates.matchesPermanentPredicate(moved, trigger.filter(), null);
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
}
