package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Registers the source permanent's one-time echo payment trigger for its controller's next upkeep.
 * The payment can be a fixed mana cost or a dynamic generic cost evaluated at that upkeep.
 */
public record RegisterEchoAtNextUpkeepEffect(String manaCost, DynamicAmount dynamicManaCost) implements CardEffect {

    public RegisterEchoAtNextUpkeepEffect(String manaCost) {
        this(manaCost, null);
    }

    public RegisterEchoAtNextUpkeepEffect(DynamicAmount dynamicManaCost) {
        this(null, dynamicManaCost);
    }
}
