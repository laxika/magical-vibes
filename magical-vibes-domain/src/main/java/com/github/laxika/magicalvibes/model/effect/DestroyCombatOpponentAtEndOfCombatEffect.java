package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Combat trigger: schedule the combat opponent (the creature this permanent blocks, or that
 * becomes blocked by this permanent) for destruction at end of combat if it matches {@code filter}.
 * Basilisk-style "Whenever this creature blocks or becomes blocked by a [filter] creature, destroy
 * that creature at end of combat." (e.g. Deathgazer's "a nonblack creature" filter).
 * <p>
 * Placed on the {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BLOCK} slot (for the
 * "blocks" half, auto-targeting the blocked attacker) and on
 * {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BECOMES_BLOCKED} with
 * {@link com.github.laxika.magicalvibes.model.TriggerMode#PER_BLOCKER} (for the "becomes blocked"
 * half, one trigger per blocker). The referenced creature is passed as the stack entry's target but
 * the trigger does not target (it can't fizzle). At resolution the opponent's colour/type is checked
 * against {@code filter}; a delayed {@link com.github.laxika.magicalvibes.model.action.DestroyPermanentAtEndOfCombat}
 * is queued only when it matches.
 *
 * @param filter               the opponent must be a creature matching this predicate to be destroyed
 * @param cannotBeRegenerated  whether the scheduled destruction ignores regeneration shields
 */
public record DestroyCombatOpponentAtEndOfCombatEffect(
        PermanentPredicate filter,
        boolean cannotBeRegenerated
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
