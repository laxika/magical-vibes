package com.github.laxika.magicalvibes.model.effect;

/**
 * "All damage that would be dealt this turn to target creature by a source of your choice is
 * dealt to another target creature instead." The source is chosen on resolution; both creatures
 * are targets of the spell.
 */
public record RedirectTargetCreatureDamageFromChosenSourceToTargetEffect(
        int protectedTargetGroup,
        int redirectTargetGroup
) implements CardEffect {

    public RedirectTargetCreatureDamageFromChosenSourceToTargetEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
