package com.github.laxika.magicalvibes.model.effect;

/** Echo's printed cost, resolved into the applicable payment cost when the trigger resolves. */
public record PayEchoCost(String echoCost, HandCardCost handCardCost, CostEffect cost) implements CostEffect {

    public PayEchoCost(String echoCost) {
        this(echoCost, null, null);
    }

    public PayEchoCost(String echoCost, HandCardCost handCardCost) {
        this(echoCost, handCardCost, null);
    }

    public PayEchoCost(HandCardCost handCardCost) {
        this(null, handCardCost, null);
    }

    public PayEchoCost(CostEffect cost) {
        this(null, null, cost);
    }
}
