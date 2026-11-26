package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Echo's printed cost, the ransom of echo's "sacrifice it unless you pay [cost]" ({@code KeywordTriggers.echo}, an
 * {@link UnlessPaysEffect}): asked and paid as the cost it names as the ability resolves, with a "rather than pay the
 * echo cost" alternative (Thick-Skinned Goblin) offered first while it applies. Exactly one cost is set.
 *
 * <p>{@code dynamicEchoCost}, when set, is a mana amount (Volcano Hellion's "echo {X}, where X is your
 * life total") evaluated at resolution, so it reflects the life total when the echo is paid rather than
 * when the upkeep trigger was put on the stack.
 */
public record PayEchoCost(String echoCost, HandCardCost handCardCost, CostEffect cost,
                          DynamicAmount dynamicEchoCost) implements CostEffect {

    public PayEchoCost(String echoCost) {
        this(echoCost, null, null, null);
    }

    public PayEchoCost(String echoCost, HandCardCost handCardCost) {
        this(echoCost, handCardCost, null, null);
    }

    public PayEchoCost(String echoCost, HandCardCost handCardCost, CostEffect cost) {
        this(echoCost, handCardCost, cost, null);
    }

    public PayEchoCost(HandCardCost handCardCost) {
        this(null, handCardCost, null, null);
    }

    public PayEchoCost(CostEffect cost) {
        this(null, null, cost, null);
    }
}
