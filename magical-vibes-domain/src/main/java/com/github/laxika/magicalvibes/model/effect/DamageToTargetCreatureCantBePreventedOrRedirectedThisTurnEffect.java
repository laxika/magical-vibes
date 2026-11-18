package com.github.laxika.magicalvibes.model.effect;

/**
 * "Damage that would be dealt to that creature this turn can't be prevented or dealt instead to another permanent or
 * player" (Whippoorwill): a floating effect on the target until end of turn.
 */
public record DamageToTargetCreatureCantBePreventedOrRedirectedThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
