package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Additional cast cost: pay a fixed amount of life or pay the listed mana cost.
 * Independent costs retain their payment units when combined. A waived base mana cost does not
 * remove optional life payments; it only removes the colored mana those payments could replace.
 */
public record PayLifeOrPayManaCost(int lifeAmount, String manaCost,
                                  List<PayLifeOrPayManaCost> paymentUnits,
                                  boolean baseManaCostWaived) implements CostEffect {
    public PayLifeOrPayManaCost {
        paymentUnits = List.copyOf(paymentUnits);
    }

    public PayLifeOrPayManaCost(int lifeAmount, String manaCost) {
        this(lifeAmount, manaCost, List.of(), false);
    }
}
