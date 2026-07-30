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
 * <p>When {@code genericIncrease} is non-null it is evaluated at resolution time and added to the
 * generic portion of {@code manaCost} — "pay {1} for each other creature you control"
 * (Fettergeist, base {@code "{0}"}).
 *
 * <p>When {@code forCumulativeUpkeep} is true, payment may spend cumulative-upkeep-only mana
 * (Adarkar Unicorn / Snowfall restriction).
 *
 * <p>When {@code lifeAmount} &gt; 0, accepting also pays that much life (Infernal Darkness:
 * "Pay {B} and 1 life" cumulative upkeep — total life = life-per-age × age counters).
 *
 * @param manaCost             base mana cost string like {@code "{G}{G}{G}{G}"} or {@code "{10}"}
 * @param genericReduction     generic-mana reduction applied at resolution, or {@code null} for none
 * @param genericIncrease      generic-mana increase applied at resolution, or {@code null} for none
 * @param forCumulativeUpkeep  whether this payment is a cumulative upkeep cost
 * @param lifeAmount           additional life to pay with the mana (0 for mana-only costs)
 */
public record PayManaCost(
        String manaCost, DynamicAmount genericReduction, DynamicAmount genericIncrease,
        boolean forCumulativeUpkeep, int lifeAmount)
        implements CostEffect {

    public PayManaCost(String manaCost) {
        this(manaCost, null, null, false, 0);
    }

    public PayManaCost(String manaCost, DynamicAmount genericReduction) {
        this(manaCost, genericReduction, null, false, 0);
    }

    public PayManaCost(String manaCost, DynamicAmount genericReduction, boolean forCumulativeUpkeep) {
        this(manaCost, genericReduction, null, forCumulativeUpkeep, 0);
    }

    public PayManaCost(String manaCost, DynamicAmount genericReduction, boolean forCumulativeUpkeep, int lifeAmount) {
        this(manaCost, genericReduction, null, forCumulativeUpkeep, lifeAmount);
    }

    /** "Pay {base} plus {1} per …" — the increase is evaluated when the ability resolves. */
    public static PayManaCost withGenericIncrease(String manaCost, DynamicAmount genericIncrease) {
        return new PayManaCost(manaCost, null, genericIncrease, false, 0);
    }

    @Override
    public int lifePaid(int currentLife) {
        return lifeAmount;
    }
}
