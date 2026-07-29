package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: this permanent and its combat opponent (the creature it blocks, or that becomes
 * blocked by it) both phase out. "Whenever this creature blocks or becomes blocked by a creature,
 * this creature and that creature phase out." (Dream Fighter).
 * <p>
 * Placed on the {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BLOCK} slot (for the
 * "blocks" half) and on {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BECOMES_BLOCKED}
 * with {@link com.github.laxika.magicalvibes.model.TriggerMode#PER_BLOCKER} (for the "becomes
 * blocked" half, one trigger per blocker). As a {@link CombatOpponentReferencingEffect} the combat
 * opponent is carried as the trigger's non-targeting target, so the trigger can't fizzle.
 * <p>
 * Both permanents leave the battlefield for {@code GameData.phasedOutPermanents} (CR 702.26b) and
 * are removed from combat (CR 506.4), so neither deals nor receives combat damage. Having phased
 * out directly, each phases in during its controller's next untap step (CR 702.26a) even though it
 * has no phasing keyword.
 */
public record PhaseOutSelfAndCombatOpponentEffect() implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
