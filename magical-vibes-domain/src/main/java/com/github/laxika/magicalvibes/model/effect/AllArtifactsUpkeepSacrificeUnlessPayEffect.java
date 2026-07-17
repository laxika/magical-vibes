package com.github.laxika.magicalvibes.model.effect;

/**
 * Energy Flux: "All artifacts have 'At the beginning of your upkeep, sacrifice this artifact
 * unless you pay {N}.'"
 *
 * <p>A static marker placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC}
 * and read by {@code StepTriggerService} during each player's upkeep: for every artifact the
 * active player controls it pushes a
 * {@link ForcedCostOrElseEffect}{@code (PayManaCost, [SacrificeSelfEffect], true)} trigger sourced
 * at that artifact. The grant is global (every artifact, regardless of who controls the
 * enchantment) and the trigger fires on each artifact's controller's own upkeep.
 *
 * <p>Not part of the layer-system board computation (no {@code LayerClassifier} entry), so the
 * layer system safely ignores it.
 */
public record AllArtifactsUpkeepSacrificeUnlessPayEffect(String manaCost) implements CardEffect {
}
