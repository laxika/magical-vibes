package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose two target creatures controlled by the same opponent. That player chooses and sacrifices
 * one of those creatures. Put a -1/-1 counter on the other." (Retribution.)
 *
 * <p>Reads both targets from the flat multi-target list. The creatures' controller — never the
 * spell's controller — makes the sacrifice choice as the spell resolves; the target that survives
 * the choice gets one -1/-1 counter.</p>
 *
 * <p>Per the card's ruling, an illegal target on resolution simply drops out of the choice: with
 * only one target left, that player must sacrifice it and no counter is placed anywhere; with none
 * left, the spell does nothing.</p>
 */
public record SacrificeOneOfTwoTargetCreaturesThenCounterOnOtherEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    /** Not single-target removal — two creatures are targeted and only one of them is sacrificed. */
    @Override
    public RemovalKind removalKind() {
        return null;
    }
}
