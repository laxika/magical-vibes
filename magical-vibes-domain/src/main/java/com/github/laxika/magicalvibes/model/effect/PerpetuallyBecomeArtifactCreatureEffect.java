package com.github.laxika.magicalvibes.model.effect;

/**
 * Permanently changes the targeted permanent's card to an artifact creature with the supplied
 * base power and toughness and haste. The handler writes a runtime card copy, so the
 * characteristics follow the card through zone changes.
 */
public record PerpetuallyBecomeArtifactCreatureEffect(int power, int toughness) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
