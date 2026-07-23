package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cumulative upkeep [cost] (CR 702.24): at the beginning of your upkeep, put an age counter on this
 * permanent, then you may pay {@code cost} for each age counter on it. If you don't, sacrifice it.
 *
 * <p>Mana costs use {@link #costPerAge} (payment flagged so cumulative-upkeep-only mana works).
 * Optional {@link #lifePerAge} adds life paid per age counter alongside the mana (Infernal Darkness
 * — "Pay {B} and 1 life"). Non-mana costs use {@link #sacrificeFilter}: sacrifice one matching
 * permanent per age counter (Polar Kraken — "Sacrifice a land").
 *
 * @param costPerAge mana cost string paid once per age counter (e.g. {@code "{U}"}), or null when
 *     the cost is a sacrifice
 * @param sacrificeFilter permanent filter for a sacrifice-per-age-counter cost, or null for mana
 * @param lifePerAge life paid once per age counter (0 when the cost is mana-only or sacrifice)
 */
public record CumulativeUpkeepEffect(
        String costPerAge, PermanentPredicate sacrificeFilter, int lifePerAge)
        implements CardEffect {

    public CumulativeUpkeepEffect {
        if ((costPerAge == null) == (sacrificeFilter == null)) {
            throw new IllegalArgumentException(
                    "Exactly one of costPerAge or sacrificeFilter must be non-null");
        }
        if (lifePerAge < 0) {
            throw new IllegalArgumentException("lifePerAge must be >= 0");
        }
        if (sacrificeFilter != null && lifePerAge > 0) {
            throw new IllegalArgumentException("lifePerAge is only valid with a mana costPerAge");
        }
    }

    /** Cumulative upkeep {mana} — e.g. {@code new CumulativeUpkeepEffect("{1}")}. */
    public CumulativeUpkeepEffect(String costPerAge) {
        this(costPerAge, null, 0);
    }

    /** Cumulative upkeep — Pay {mana} and N life (Infernal Darkness). */
    public CumulativeUpkeepEffect(String costPerAge, int lifePerAge) {
        this(costPerAge, null, lifePerAge);
    }

    /** Cumulative upkeep — sacrifice a permanent matching {@code filter} per age counter. */
    public static CumulativeUpkeepEffect sacrifice(PermanentPredicate filter) {
        return new CumulativeUpkeepEffect(null, filter, 0);
    }

    public boolean isSacrificeCost() {
        return sacrificeFilter != null;
    }
}
