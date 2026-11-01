package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;

import java.util.Map;

/**
 * The {@code Trigger} each slot of a family in shadow mode becomes when that family moves to the trigger
 * pipeline (step 1 of each family's move in effect-slot-refactor.md). Shadow mode matches a slot's abilities as
 * if they had this trigger.
 */
public final class LegacySlotTriggers {

    private static final Map<EffectSlot, ZoneChangeTrigger> TRIGGERS = Map.of(
            EffectSlot.ON_DEATH, Triggers.dies(ObjectRelation.THIS),
            EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, Triggers.leavesBattlefield(ObjectRelation.THIS),
            EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
            Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS),
            EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
            Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS),
            EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_HAND,
            Triggers.putIntoGraveyardFrom(Zone.HAND, ObjectRelation.THIS),
            EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, ObjectRelation.THIS),
            EffectSlot.ON_SELF_MILLED, Triggers.milled(ObjectRelation.THIS));

    private LegacySlotTriggers() {
    }

    /** The trigger {@code slot} becomes, or {@code null} for a slot no step has mapped yet. */
    public static ZoneChangeTrigger of(EffectSlot slot) {
        return TRIGGERS.get(slot);
    }

    /**
     * The trigger {@code effect}, a registration in {@code slot}, becomes. ON_DEATH also holds "when you sacrifice
     * this", which a sacrifice triggers wherever the card goes: a replacement that exiles it instead doesn't undo
     * the sacrifice.
     */
    public static ZoneChangeTrigger of(EffectSlot slot, CardEffect effect) {
        if (slot == EffectSlot.ON_DEATH && effect.onlyTriggersOnSacrifice()) {
            return Triggers.sacrificed(ObjectRelation.THIS);
        }
        return of(slot);
    }
}
