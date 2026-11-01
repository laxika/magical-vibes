package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;

/** Decides whether a rules event meets a candidate ability's trigger condition. No per-slot code. */
final class TriggerMatcher {

    private TriggerMatcher() {
    }

    /**
     * The first of the ability's trigger conditions that {@code event} meets, or {@code null}. A condition is met
     * when its trigger is a zone change of this kind, the moving object relates to the source as the trigger says,
     * and the source is in a zone the condition functions from (CR 113.6). Only {@code THIS} is matched until
     * step 11.
     */
    static ZoneChangeTrigger matchedTrigger(TriggerCandidate candidate, ZoneChangeEvent event) {
        for (TriggerCondition condition : candidate.ability().conditions()) {
            if (condition.trigger() instanceof ZoneChangeTrigger trigger
                    && matches(trigger, condition.zones().functionsIn(candidate.zone()), event)) {
                return trigger;
            }
        }
        return null;
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
