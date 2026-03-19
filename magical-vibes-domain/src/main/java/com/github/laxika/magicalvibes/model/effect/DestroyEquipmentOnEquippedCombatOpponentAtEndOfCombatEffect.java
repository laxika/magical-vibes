package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger effect for creatures like Corrosive Ooze:
 * "Whenever this creature blocks or becomes blocked by an equipped creature,
 * destroy all Equipment attached to that creature at end of combat."
 * <p>
 * At trigger creation time (in CombatBlockService), the combat opponent is checked for attached
 * Equipment. If equipped, the trigger is placed on the stack with the equipped creature as the
 * target. When the trigger resolves, the target creature's ID is recorded in
 * {@code GameData.creaturesWithEquipmentToDestroyAtEndOfCombat}. At end of combat, all Equipment
 * currently attached to those creatures is destroyed.
 * <p>
 * Use with {@code EffectSlot.ON_BLOCK} (non-per-blocker) and
 * {@code EffectSlot.ON_BECOMES_BLOCKED} ({@code TriggerMode.PER_BLOCKER}).
 */
public record DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect() implements CardEffect {
}
