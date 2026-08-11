package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose two target creatures controlled by the same player. Their controller chooses and
 * sacrifices one of them. Return the other to its owner's hand." (Barrin's Spite.)
 *
 * <p>Reads both targets from the flat multi-target list. The creatures' controller makes the
 * sacrifice choice as the spell resolves; the other target is then returned to its owner's hand.</p>
 */
public record SacrificeOneOfTwoTargetCreaturesThenReturnOtherToHandEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    /** Not single-target removal — two creatures are targeted and only one is sacrificed. */
    @Override
    public RemovalKind removalKind() {
        return null;
    }
}
