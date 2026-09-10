package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Optional spell cost that taps creatures with total effective power at least the threshold. */
public record TeamworkCost(int requiredPower) implements PowerBasedTapCost {

    public TeamworkCost {
        if (requiredPower < 0) {
            throw new IllegalArgumentException("Teamwork power cannot be negative");
        }
    }

    @Override
    public String paymentNoun() {
        return "teamwork";
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return new PermanentIsCreaturePredicate();
    }
}
