package com.github.laxika.magicalvibes.model.effect;

/** Cost that taps untapped creatures with a combined effective power threshold. */
public interface PowerBasedTapCost extends CostEffect {

    /** The minimum total power required from the tapped creatures. */
    int requiredPower();

    /** The action named in payment prompts and game logs. */
    default String paymentNoun() {
        return "crew";
    }
}
