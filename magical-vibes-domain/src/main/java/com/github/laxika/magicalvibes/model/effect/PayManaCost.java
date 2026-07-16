package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Pay {cost}" as the payable side of a {@link ForcedCostOrElseEffect}. Because paying mana is
 * inherently a choice, it is only meaningful with {@code optional = true} ("you may pay {cost};
 * if you don't, [penalty]"). Used by Force of Nature ("pay {G}{G}{G}{G} or take 8 damage").
 *
 * <p>When {@code genericReduction} is non-null it is evaluated at resolution time and subtracted
 * from the generic portion of {@code manaCost} (floored at 0) — "this cost is reduced by {2} for
 * each basic land type among lands you control" (Draco).
 *
 * @param manaCost         base mana cost string like {@code "{G}{G}{G}{G}"} or {@code "{10}"}
 * @param genericReduction generic-mana reduction applied at resolution, or {@code null} for none
 */
public record PayManaCost(String manaCost, DynamicAmount genericReduction) implements CostEffect {

    public PayManaCost(String manaCost) {
        this(manaCost, null);
    }
}
