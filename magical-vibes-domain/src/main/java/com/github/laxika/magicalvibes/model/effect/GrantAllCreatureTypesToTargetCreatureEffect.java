package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect that makes the targeted creature every creature type, without giving it the
 * changeling keyword ("Up to one target creature gains all creature types. This effect doesn't end.").
 * The types are added to the permanent's {@code grantedSubtypes}, so the creature does not gain an
 * ability.
 */
public record GrantAllCreatureTypesToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
