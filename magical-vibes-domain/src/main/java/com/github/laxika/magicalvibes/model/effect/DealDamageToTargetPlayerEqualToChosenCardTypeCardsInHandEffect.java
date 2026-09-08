package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a card type, then the target player reveals their hand and this effect
 * deals damage to that player for each revealed card of the chosen type.
 */
public record DealDamageToTargetPlayerEqualToChosenCardTypeCardsInHandEffect(int damagePerCard)
        implements CardEffect {

    public DealDamageToTargetPlayerEqualToChosenCardTypeCardsInHandEffect() {
        this(3);
    }

    public DealDamageToTargetPlayerEqualToChosenCardTypeCardsInHandEffect {
        if (damagePerCard < 0) {
            throw new IllegalArgumentException("damagePerCard must not be negative");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
