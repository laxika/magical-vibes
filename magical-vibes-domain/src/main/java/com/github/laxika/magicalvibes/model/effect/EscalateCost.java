package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A modal spell's additional cost that depends on how many modes are chosen, paid as the total cost is
 * determined (CR 601.2f):
 * <ul>
 *   <li>{@link Kind#ESCALATE}: "Escalate [cost]", paid once for each mode chosen beyond the first (CR 702.120a);</li>
 *   <li>{@link Kind#ENTWINE}: "Entwine [cost]", paid once when all modes are chosen instead of the number specified
 *   (CR 702.42a).</li>
 * </ul>
 * One payment is {@code perModeCost}, one of: a mana cost ({@link PayManaCost}, Escalate {1}); "discard a card"
 * ({@link DiscardCardTypeCost} of any one card); "sacrifice N [permanents]" ({@link SacrificeMultiplePermanentsCost},
 * Entwine—Sacrifice three lands); "tap an untapped [permanent] you control" ({@link TapMultiplePermanentsCost} of
 * one). Mana is paid with the spell's mana; the hand and permanent payments are chosen by the caster and carried by
 * the cast request ({@code discardHandCardIndices}, {@code additionalCostSacrificePermanentIds}). A spell cast
 * without paying its mana cost still pays it (CR 118.9d).
 *
 * @param perModeCost what one payment costs
 * @param kind        when the payments are owed
 */
public record EscalateCost(CostEffect perModeCost, Kind kind) implements CostEffect {

    /** When the per-mode cost is owed. */
    public enum Kind {
        /** Once for each mode chosen beyond the first. */
        ESCALATE,
        /** Once when every mode is chosen instead of the number specified. */
        ENTWINE
    }

    public EscalateCost {
        if (kind == null) {
            throw new IllegalArgumentException("kind is required");
        }
        boolean supported = switch (perModeCost) {
            case PayManaCost mana -> mana.manaCost() != null && !mana.manaCost().isEmpty();
            case DiscardCardTypeCost discard -> discard.predicate() == null && discard.count() == 1
                    && !discard.manaValueEqualsX() && !discard.sameName();
            case SacrificeMultiplePermanentsCost sacrifice -> sacrifice.count() >= 1;
            case TapMultiplePermanentsCost tap -> tap.count() instanceof Fixed fixed && fixed.value() == 1
                    && !tap.excludeSource();
            case null, default -> false;
        };
        if (!supported) {
            throw new IllegalArgumentException("Unsupported per-mode cost: " + perModeCost);
        }
    }

    /** "Escalate {cost}". */
    public static EscalateCost escalate(String manaCost) {
        return new EscalateCost(new PayManaCost(manaCost), Kind.ESCALATE);
    }

    /** "Escalate—[cost]". */
    public static EscalateCost escalate(CostEffect perModeCost) {
        return new EscalateCost(perModeCost, Kind.ESCALATE);
    }

    /** "Entwine {cost}". */
    public static EscalateCost entwine(String manaCost) {
        return new EscalateCost(new PayManaCost(manaCost), Kind.ENTWINE);
    }

    /** "Entwine—[cost]". */
    public static EscalateCost entwine(CostEffect perModeCost) {
        return new EscalateCost(perModeCost, Kind.ENTWINE);
    }

    /**
     * How many times the per-mode cost is paid when {@code modesChosen} modes are chosen of a spell whose mode
     * choice asks for {@code requiredModes}.
     */
    public int payments(int modesChosen, int requiredModes) {
        return switch (kind) {
            case ESCALATE -> Math.max(0, modesChosen - 1);
            case ENTWINE -> modesChosen > requiredModes ? 1 : 0;
        };
    }

    /** The mana one payment costs, or null when the per-mode cost isn't mana. */
    public String manaCost() {
        return perModeCost instanceof PayManaCost mana ? mana.manaCost() : null;
    }

    /** Whether one payment discards a card. */
    public boolean discards() {
        return perModeCost instanceof DiscardCardTypeCost;
    }

    /** Whether one payment sacrifices permanents. */
    public boolean sacrifices() {
        return perModeCost instanceof SacrificeMultiplePermanentsCost;
    }

    /** Whether one payment taps a permanent. */
    public boolean taps() {
        return perModeCost instanceof TapMultiplePermanentsCost;
    }

    /** How many permanents one payment sacrifices or taps (0 for mana and discard). */
    public int permanentsPerPayment() {
        return switch (perModeCost) {
            case SacrificeMultiplePermanentsCost sacrifice -> sacrifice.count();
            case TapMultiplePermanentsCost ignored -> 1;
            default -> 0;
        };
    }

    /** The keyword's word for log lines and prompts: "escalate" or "entwine". */
    public String verb() {
        return kind == Kind.ESCALATE ? "escalate" : "entwine";
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return perModeCost.consumedPermanentFilter();
    }
}
