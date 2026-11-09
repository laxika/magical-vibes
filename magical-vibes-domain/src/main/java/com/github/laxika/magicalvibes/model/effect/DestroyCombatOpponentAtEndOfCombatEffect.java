package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule "that creature" — the creature the source blocks, or that becomes blocked by it — for
 * destruction at end of combat. Basilisk-style "Whenever this creature blocks or becomes blocked by a [filter] creature,
 * destroy that creature at end of combat" (e.g. Deathgazer's "a nonblack creature").
 * <p>
 * Placed on the {@code Triggers.blocks()} ability (the "blocks" half) and on the {@code Triggers.becomesBlocked()}
 * ability with {@code TriggerOptions.EACH_BLOCKER} (the "becomes blocked" half, one trigger per blocker); the
 * "[filter] creature" is the trigger's filter on the creature on the other side of the block ({@code withFilter}),
 * judged as it blocks or becomes blocked (CR 509.3f). The card declares no target for it, so the collector records the
 * other creature as the entry's triggering permanent and non-targeting target (it can't fizzle), which the effect
 * reads as it resolves.
 *
 * @param cannotBeRegenerated           whether the scheduled destruction ignores regeneration shields
 * @param putCounterOnSourceIfDestroyed whether the remembered source creature gets a +1/+1
 *                                      counter at the next end step if the opponent is destroyed
 */
public record DestroyCombatOpponentAtEndOfCombatEffect(
        boolean cannotBeRegenerated,
        boolean putCounterOnSourceIfDestroyed
) implements CardEffect {

    public DestroyCombatOpponentAtEndOfCombatEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
