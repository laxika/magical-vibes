package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals X damage to any target (creature or player). When {@code exileInsteadOfDie} is true,
 * if the target is a creature that was dealt damage this way and it would die this turn,
 * exile it instead (replacement effect, CR 614.6).
 */
public record DealXDamageToAnyTargetEffect(boolean exileInsteadOfDie) implements CardEffect {

    public DealXDamageToAnyTargetEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
