package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;

import java.util.Map;

/**
 * The {@code Trigger} each slot of a family in shadow mode becomes when that family moves to the trigger
 * pipeline (step 1 of each family's move in effect-slot-refactor.md). Shadow mode matches a slot's abilities as
 * if they had this trigger. No family is in shadow mode now: the pilot's slots moved in step 10b and the
 * creature-death slots in step 11, which ported their collectors rather than shadowing them.
 */
public final class LegacySlotTriggers {

    private static final Map<EffectSlot, ZoneChangeTrigger> TRIGGERS = Map.of();

    private LegacySlotTriggers() {
    }

    /** The trigger {@code slot} becomes, or {@code null} for a slot no step has mapped yet. */
    public static ZoneChangeTrigger of(EffectSlot slot) {
        return TRIGGERS.get(slot);
    }

    /** The trigger {@code effect}, a registration in {@code slot}, becomes. */
    public static ZoneChangeTrigger of(EffectSlot slot, CardEffect effect) {
        return of(slot);
    }
}
