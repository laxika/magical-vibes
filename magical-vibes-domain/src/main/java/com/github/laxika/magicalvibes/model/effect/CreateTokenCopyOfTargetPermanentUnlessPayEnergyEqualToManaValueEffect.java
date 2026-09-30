package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token copy of the targeted permanent and schedules that token to be sacrificed at
 * the controller's next end step unless its controller pays energy equal to its mana value.
 */
public record CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect(
        CreateTokenCopyOfTargetPermanentEffect copyEffect) implements CardEffect {

    public CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect(false, false, false, true));
    }

    public CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect(
            CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        this.copyEffect = java.util.Objects.requireNonNull(copyEffect, "copyEffect");
        if (copyEffect.chooseAttackTarget()) {
            throw new IllegalArgumentException("The attack target must be inherited from the source");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return copyEffect.targetSpec();
    }
}
