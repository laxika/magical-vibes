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
 * <p>When {@code payerIsEnchantedController} is true, the prompt goes to the player on the stack
 * entry's {@code targetId} instead of the source's controller. That covers both the enchanted
 * permanent's controller under {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} (Mind Whip:
 * "that player may pay {3}") and the active player under {@code EACH_UPKEEP_TRIGGERED} (Pillar
 * Tombs of Aku: "that player may sacrifice a creature"). Mutually exclusive with
 * {@code anyPlayerMayPay}.
 *
 * <p>When {@code payerIsDefendingPlayer} is true the prompt goes to the defending player of the
 * attack that triggered the ability — the attacked player, or the attacked planeswalker's
 * controller, read from the {@code ON_ATTACK} trigger's {@code attackedTargetId} (Ogre Marauder:
 * "unless defending player sacrifices a creature of their choice"). Mutually exclusive with the
 * other payer flags.
 */
public record ForcedCostOrElseEffect(
        CostEffect forcedCost,
        List<CardEffect> elseEffects,
        boolean optional,
        boolean anyPlayerMayPay,
        boolean payerIsEnchantedController,
        boolean payerIsDefendingPlayer
) implements CardEffect {
    public ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects) {
        this(forcedCost, elseEffects, false, false, false, false);
    }

    public ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects, boolean optional) {
        this(forcedCost, elseEffects, optional, false, false, false);
    }

    public ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects, boolean optional,
                                  boolean anyPlayerMayPay) {
        this(forcedCost, elseEffects, optional, anyPlayerMayPay, false, false);
    }

    /**
     * "…unless defending player [pays the cost]" on an {@code ON_ATTACK} trigger — the defending
     * player is asked, and declining (or being unable to pay) resolves the else effects.
     */
    public static ForcedCostOrElseEffect defendingPlayerMayPay(CostEffect forcedCost,
                                                               List<CardEffect> elseEffects) {
        return new ForcedCostOrElseEffect(forcedCost, elseEffects, true, false, false, true);
    }

    /**
     * "That player may pay {cost}. If they don't, [penalty]" where "that player" is the stack
     * entry's {@code targetId} (enchanted permanent's controller for Mind Whip; active player for
     * each-upkeep triggers like Pillar Tombs of Aku).
     */
    public static ForcedCostOrElseEffect enchantedControllerMayPay(CostEffect forcedCost,
                                                                   List<CardEffect> elseEffects) {
        return new ForcedCostOrElseEffect(forcedCost, elseEffects, true, false, true, false);
    }
}
