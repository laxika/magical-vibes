package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;

/**
 * A mana pool used by the AI for planning purposes. Extends {@link ManaPool} with
 * tracking for over-counted mana from sources whose mana abilities are mutually
 * exclusive (only one ability activates per tap).
 * <p>
 * When building a virtual pool, a source with multiple free-tap mana abilities
 * (e.g. a dual land producing R or G, a pain land producing C, R, or G) contributes
 * mana for <em>all</em> of its ability colors, but can only be tapped once. Two
 * forms of over-counting are tracked:
 * <ul>
 *     <li>{@code flexibleOvercount} &mdash; the inflation of {@link #getTotal()}
 *         across all sources. Each source inflates the total by
 *         {@code (sum of amounts across abilities) - (max amount from any single ability)}.</li>
 *     <li>{@code perColorOvercount} &mdash; the inflation of {@link #get(ManaColor)}
 *         per color. Only non-zero when a single source has multiple abilities
 *         producing the same color; for typical duals and pain lands this is 0.</li>
 * </ul>
 * The overrides of {@link #get(ManaColor)} and {@link #getTotal()} subtract these
 * corrections so that {@link ManaCost#canPay} sees the actual realizable mana.
 */
public class VirtualManaPool extends ManaPool {

    private int flexibleOvercount;
    private final EnumMap<ManaColor, Integer> perColorOvercount;

    public VirtualManaPool() {
        super();
        this.perColorOvercount = new EnumMap<>(ManaColor.class);
    }

    /**
     * Copy constructor for deep-copying a virtual mana pool (e.g. hypothetical land evaluation).
     */
    public VirtualManaPool(VirtualManaPool source) {
        super(source);
        this.flexibleOvercount = source.flexibleOvercount;
        this.perColorOvercount = new EnumMap<>(source.perColorOvercount);
    }

    public void addFlexibleOvercount(int amount) {
        flexibleOvercount += amount;
    }

    public int getFlexibleOvercount() {
        return flexibleOvercount;
    }

    public void addPerColorOvercount(ManaColor color, int amount) {
        if (amount <= 0) {
            return;
        }
        perColorOvercount.merge(color, amount, Integer::sum);
    }

    public int getPerColorOvercount(ManaColor color) {
        return perColorOvercount.getOrDefault(color, 0);
    }

    /**
     * Returns the effective amount of the given color available for payment, correcting
     * for mutually-exclusive abilities on the same source that contribute to this color.
     * For sources where every ability produces a different color (typical dual/pain land),
     * the correction is 0 and this matches the raw added amount.
     */
    @Override
    public int get(ManaColor color) {
        return Math.max(0, super.get(color) - getPerColorOvercount(color));
    }

    /**
     * Returns the total mana adjusted for over-counting from mutually-exclusive
     * mana abilities. Each source contributes {@code max ability amount} to the
     * effective total, not the sum of all its abilities.
     */
    @Override
    public int getTotal() {
        return Math.max(0, super.getTotal() - flexibleOvercount);
    }
}
