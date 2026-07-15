package com.github.laxika.magicalvibes.model.effect;

/**
 * Desertion: "Counter target spell. If an artifact or creature spell is countered this way, put that
 * card onto the battlefield under your control instead of into its owner's graveyard."
 */
public record CounterSpellAndGainControlIfArtifactOrCreatureEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}
