package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Trigger descriptor for {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_SELF_CAST}:
 * "When you cast this spell, copy it if {@code condition}." The copy can optionally be offered
 * as a resolution-time may choice. A cast-time condition models wording such as "while you
 * control a creature", which is checked when casting finishes rather than when the trigger
 * resolves.
 * <p>
 * At cast time {@code TriggerCollectionService.checkSpellCastTriggers} snapshots the just-cast spell
 * and queues a triggered ability wrapping a {@link CopyControllerCastSpellEffect} in a
 * {@link ConditionalEffect} keyed on {@code condition}, so resolution-time conditions are checked
 * when the copy trigger resolves. Cast-time conditions are checked before the trigger is queued.
 * Used by the SOS Infusion copy cycle (e.g. Lumaret's Favor) and Social Snub.
 *
 * @param condition condition checked when the self-cast trigger resolves
 * @param optional whether the controller may decline to create the copy
 * @param conditionAtCast whether the condition is checked when casting finishes instead
 */
public record CopyThisSpellIfConditionEffect(Condition condition, boolean optional, boolean conditionAtCast)
        implements CardEffect {

    public CopyThisSpellIfConditionEffect(Condition condition) {
        this(condition, false, false);
    }

    public CopyThisSpellIfConditionEffect(Condition condition, boolean optional) {
        this(condition, optional, false);
    }

    /** Creates a copy trigger whose condition is checked when casting finishes. */
    public static CopyThisSpellIfConditionEffect whenCastWhile(Condition condition, boolean optional) {
        return new CopyThisSpellIfConditionEffect(condition, optional, true);
    }
}
