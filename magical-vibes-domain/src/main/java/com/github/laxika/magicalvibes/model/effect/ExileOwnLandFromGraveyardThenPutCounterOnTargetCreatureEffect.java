package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time "you may exile a land card from your graveyard. If you do, put a +1/+1 counter
 * on target creature" effect. The creature target is chosen when the triggered ability is put on
 * the stack; the land exile and counter placement are contingent on accepting the may choice.
 */
public record ExileOwnLandFromGraveyardThenPutCounterOnTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
