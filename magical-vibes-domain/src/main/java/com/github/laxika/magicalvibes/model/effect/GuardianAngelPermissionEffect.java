package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the spell's controller a repeatable special action to pay {@code {1}} and prevent the
 * next 1 damage to the spell's target until end of turn.
 */
public record GuardianAngelPermissionEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyTarget());
    }
}
