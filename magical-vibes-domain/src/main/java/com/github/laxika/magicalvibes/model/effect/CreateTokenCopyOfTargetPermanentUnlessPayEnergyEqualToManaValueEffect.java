package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token copy of the targeted permanent and schedules that token to be sacrificed at
 * the controller's next end step unless its controller pays energy equal to its mana value. A tapped and
 * attacking copy's controller chooses what it attacks (CR 508.4).
 */
public record CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect(
        CreateTokenCopyOfTargetPermanentEffect copyEffect) implements CardEffect {

    public CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect(false, false, false, true));
    }

    public CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect(
            CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        this.copyEffect = java.util.Objects.requireNonNull(copyEffect, "copyEffect");
    }

    @Override
    public TargetSpec targetSpec() {
        return copyEffect.targetSpec();
    }
}
