package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Performs a cost-like action as an effect instruction. If it cannot be performed,
 * resolves the fallback effects.
 *
 * <p>When {@code optional} is true the action is a "you may" choice ("you may sacrifice an
 * artifact. If you don't, ..."): the controller is asked, and declining (or being unable to
 * pay) resolves the fallback effects. When false the action is mandatory and the fallback only
 * fires if the cost cannot be paid at all (e.g. Archdemon of Greed).
 *
 * <p>When {@code anyPlayerMayPay} is true (Icy Prison: "sacrifice this unless any player pays
 * {3}"), players are prompted in APNAP order; the first to pay satisfies the cost and stops the
 * sequence, and only if every player declines (or can't pay) do the fallback effects resolve.
 * Currently only meaningful with an optional {@link PayManaCost}.
 *
 * <p>When {@code payerIsEnchantedController} is true (Mind Whip: "that player may pay {3}. If
 * they don't, ..."), the prompt goes to the enchanted permanent's controller — the player carried
 * on the stack entry's {@code targetId} by an
 * {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} trigger — instead of the Aura's
 * controller. Mutually exclusive with {@code anyPlayerMayPay}.
 */
public record ForcedCostOrElseEffect(
        CostEffect forcedCost,
        List<CardEffect> elseEffects,
        boolean optional,
        boolean anyPlayerMayPay,
        boolean payerIsEnchantedController
) implements CardEffect {
    public ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects) {
        this(forcedCost, elseEffects, false, false, false);
    }

    public ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects, boolean optional) {
        this(forcedCost, elseEffects, optional, false, false);
    }

    public ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects, boolean optional,
                                  boolean anyPlayerMayPay) {
        this(forcedCost, elseEffects, optional, anyPlayerMayPay, false);
    }

    /**
     * "That player may pay {cost}. If they don't, [penalty]" where "that player" is the enchanted
     * permanent's controller (Mind Whip).
     */
    public static ForcedCostOrElseEffect enchantedControllerMayPay(CostEffect forcedCost,
                                                                   List<CardEffect> elseEffects) {
        return new ForcedCostOrElseEffect(forcedCost, elseEffects, true, false, true);
    }
}
