package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the spell's controller a repeatable special action to pay {@code {1}} and prevent the
 * next 1 damage to the spell's target until end of turn.
 *
 * <p>As a floating effect on the spell's controller (CR 611.2a) it names the target the resolution chose.
 *
 * @param targetId the target the permission prevents damage to; {@code null} on the effect a card carries
 */
public record GuardianAngelPermissionEffect(java.util.UUID targetId) implements CardEffect {

    public GuardianAngelPermissionEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyTarget());
    }
}
