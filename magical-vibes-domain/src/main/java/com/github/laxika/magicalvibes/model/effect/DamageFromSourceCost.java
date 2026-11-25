package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.Objects;

/**
 * "Have [this spell or permanent] deal {@code amount} damage to you" as a cost (Molten Influence's ransom). The damage
 * is dealt by the object whose effect asks for it, as it exists or last existed (CR 608.2h); the cost is paid once the
 * payer chose to pay, whether or not the damage is prevented (CR 118.12), so it is always payable.
 *
 * @param amount the damage, read as the effect resolves
 */
public record DamageFromSourceCost(DynamicAmount amount) implements CostEffect {

    public DamageFromSourceCost {
        Objects.requireNonNull(amount, "amount");
    }

    /** "Have [this] deal {@code amount} damage to you." */
    public static DamageFromSourceCost of(int amount) {
        return new DamageFromSourceCost(new Fixed(amount));
    }
}
