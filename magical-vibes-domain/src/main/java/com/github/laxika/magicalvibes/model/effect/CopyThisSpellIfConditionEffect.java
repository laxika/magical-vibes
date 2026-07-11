package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Trigger descriptor for {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_SELF_CAST}:
 * "When you cast this spell, copy it if {@code condition}. You may choose new targets for the copy."
 * <p>
 * At cast time {@code TriggerCollectionService.checkSpellCastTriggers} snapshots the just-cast spell
 * and queues a triggered ability wrapping a {@link CopyControllerCastSpellEffect} in a
 * {@link ConditionalEffect} keyed on {@code condition}, so the copy is created (with an optional
 * choose-new-targets prompt) only when the condition holds at resolution. Used by the SOS Infusion
 * copy cycle (e.g. Lumaret's Favor).
 */
public record CopyThisSpellIfConditionEffect(Condition condition) implements CardEffect {
}
