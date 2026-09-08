package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;

/**
 * Registers the source permanent's one-time echo payment trigger for its controller's next upkeep.
 * The payment can be a fixed mana cost, a dynamic generic cost evaluated at that upkeep, a
 * hand-card cost such as discarding a card, or another {@link CostEffect} such as sacrificing
 * permanents.
 */
public record RegisterEchoAtNextUpkeepEffect(String manaCost, DynamicAmount dynamicManaCost,
                                             HandCardCost handCardCost,
                                             CostEffect cost,
                                             List<CardEffect> paidEffects) implements CardEffect {

    public RegisterEchoAtNextUpkeepEffect {
        paidEffects = paidEffects == null ? List.of() : List.copyOf(paidEffects);
    }

    public RegisterEchoAtNextUpkeepEffect(String manaCost) {
        this(manaCost, null, null, null, List.of());
    }

    public RegisterEchoAtNextUpkeepEffect(String manaCost, List<CardEffect> paidEffects) {
        this(manaCost, null, null, null, paidEffects);
    }

    public RegisterEchoAtNextUpkeepEffect(DynamicAmount dynamicManaCost) {
        this(null, dynamicManaCost, null, null, List.of());
    }

    public RegisterEchoAtNextUpkeepEffect(DynamicAmount dynamicManaCost, List<CardEffect> paidEffects) {
        this(null, dynamicManaCost, null, null, paidEffects);
    }

    public RegisterEchoAtNextUpkeepEffect(HandCardCost handCardCost) {
        this(null, null, handCardCost, null, List.of());
    }

    public RegisterEchoAtNextUpkeepEffect(HandCardCost handCardCost, List<CardEffect> paidEffects) {
        this(null, null, handCardCost, null, paidEffects);
    }

    public RegisterEchoAtNextUpkeepEffect(CostEffect cost) {
        this(null, null, null, cost, List.of());
    }

    public RegisterEchoAtNextUpkeepEffect(CostEffect cost, List<CardEffect> paidEffects) {
        this(null, null, null, cost, paidEffects);
    }
}
