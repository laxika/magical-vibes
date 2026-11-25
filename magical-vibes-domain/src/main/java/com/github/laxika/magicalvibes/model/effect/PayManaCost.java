package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Pay {cost}" as the payable side of a {@link ForcedCostOrElseEffect}. Because paying mana is
 * inherently a choice, it is only meaningful with {@code optional = true} ("you may pay {cost};
 * if you don't, [penalty]"). Used by Force of Nature ("pay {G}{G}{G}{G} or take 8 damage"), and as the
 * ransom of an {@link UnlessPaysEffect} ("counter target spell unless its controller pays {3}").
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
 * <p>When {@code announcedX} is true the cost is {@code "{X}"}, X being the value announced as the spell or ability was
 * put on the stack ({@link #x()}: Condescend, Power Sink, "unless its controller pays {X}"); mana that can be spent only
 * on costs with {X} pays it (Rosheen Meanderer).
 *
 * @param manaCost             base mana cost string like {@code "{G}{G}{G}{G}"} or {@code "{10}"}
 * @param genericReduction     generic-mana reduction applied at resolution, or {@code null} for none
 * @param genericIncrease      generic-mana increase applied at resolution, or {@code null} for none
 * @param forCumulativeUpkeep  whether this payment is a cumulative upkeep cost
 * @param lifeAmount           additional life to pay with the mana (0 for mana-only costs)
 * @param useSourceManaCost    whether to use the source permanent's mana cost at resolution
 * @param announcedX           whether the cost is {X}, the announced X
 * @param useTriggeringSpellManaCost whether the cost is the mana cost of the spell that caused the trigger, read as it
 *                             resolves ({@link #ofTriggeringSpell()})
 */
public record PayManaCost(
        String manaCost, DynamicAmount genericReduction, DynamicAmount genericIncrease,
        boolean forCumulativeUpkeep, int lifeAmount, boolean useSourceManaCost, boolean announcedX,
        boolean useTriggeringSpellManaCost)
        implements CostEffect {

    public PayManaCost(String manaCost, DynamicAmount genericReduction, DynamicAmount genericIncrease,
                       boolean forCumulativeUpkeep, int lifeAmount, boolean useSourceManaCost, boolean announcedX) {
        this(manaCost, genericReduction, genericIncrease, forCumulativeUpkeep, lifeAmount, useSourceManaCost,
                announcedX, false);
    }

    public PayManaCost(String manaCost, DynamicAmount genericReduction, DynamicAmount genericIncrease,
                       boolean forCumulativeUpkeep, int lifeAmount, boolean useSourceManaCost) {
        this(manaCost, genericReduction, genericIncrease, forCumulativeUpkeep, lifeAmount, useSourceManaCost, false);
    }

    public PayManaCost(String manaCost) {
        this(manaCost, null, null, false, 0, false);
    }

    public PayManaCost(String manaCost, DynamicAmount genericReduction) {
        this(manaCost, genericReduction, null, false, 0, false);
    }

    public PayManaCost(String manaCost, DynamicAmount genericReduction, boolean forCumulativeUpkeep) {
        this(manaCost, genericReduction, null, forCumulativeUpkeep, 0, false);
    }

    public PayManaCost(String manaCost, DynamicAmount genericReduction, boolean forCumulativeUpkeep, int lifeAmount) {
        this(manaCost, genericReduction, null, forCumulativeUpkeep, lifeAmount, false);
    }

    /** "Pay {base} plus {1} per …" — the increase is evaluated when the ability resolves. */
    public static PayManaCost withGenericIncrease(String manaCost, DynamicAmount genericIncrease) {
        return new PayManaCost(manaCost, null, genericIncrease, false, 0, false);
    }

    /** "Pay its mana cost" — the source permanent's current mana cost is used at resolution. */
    public static PayManaCost forSourceManaCost() {
        return new PayManaCost(null, null, null, false, 0, true);
    }

    /** "Pay {X}", X announced as the spell or ability was put on the stack. */
    public static PayManaCost x() {
        return new PayManaCost("{X}", null, null, false, 0, false, true);
    }

    /**
     * "Pay that spell's mana cost" (Ice Cave): the mana cost of the spell that caused the trigger, as it is on the stack
     * or as it last was there, with its announced X, without cost increases or reductions (Ice Cave's ruling). A spell
     * cast face down has no mana cost (CR 708.2), and a spell with no mana cost can't have it paid (CR 118.6); {0} can
     * always be paid (CR 118.3a).
     */
    public static PayManaCost ofTriggeringSpell() {
        return new PayManaCost(null, null, null, false, 0, false, false, true);
    }

    @Override
    public int lifePaid(int currentLife) {
        return lifeAmount;
    }
}
