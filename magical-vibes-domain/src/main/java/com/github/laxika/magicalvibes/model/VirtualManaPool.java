package com.github.laxika.magicalvibes.model;

/**
 * A mana pool used by the AI for planning purposes. Extends {@link ManaPool} with
 * tracking for over-counted mana from dual lands.
 * <p>
 * When building a virtual pool, dual lands add all their possible colors
 * (e.g. R+G for Rootbound Crag) but the land can only be tapped once,
 * so the raw total is inflated by (colors-1) per dual land. The
 * {@code flexibleOvercount} field tracks this inflation so that
 * {@link #getTotal()} and {@link #getEffectiveTotal()} return the
 * actual available mana.
 */
public class VirtualManaPool extends ManaPool {

    private int flexibleOvercount;

    public VirtualManaPool() {
        super();
    }

    /**
     * Copy constructor for deep-copying a virtual mana pool (e.g. hypothetical land evaluation).
     */
    public VirtualManaPool(VirtualManaPool source) {
        super(source);
        this.flexibleOvercount = source.flexibleOvercount;
    }

    public void addFlexibleOvercount(int amount) {
        flexibleOvercount += amount;
    }

    public int getFlexibleOvercount() {
        return flexibleOvercount;
    }

    /**
     * Returns the total mana adjusted for dual-land over-counting.
     * Each dual land contributes mana to multiple colors in the virtual pool,
     * but can only be tapped once. This override ensures all callers
     * (including {@link ManaCost#canPay}) see the correct effective total.
     */
    @Override
    public int getTotal() {
        return super.getTotal() - flexibleOvercount;
    }
}
