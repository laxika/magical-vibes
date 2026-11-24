package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "As this permanent enters, sacrifice any number of permanents you control matching {@code filter}", and what the
 * sacrifice gives it ({@link Payoff}): {@code countersPerPermanent} +1/+1 counters for each one (Shimatsu the
 * Bloodcloaked, Caprichrome), base power and toughness equal to their total power and toughness (Dracoplasm), or power
 * and toughness equal to the number sacrificed (Wood Elemental).
 * <p>
 * An as-enters replacement effect (CR 614.1c): a static ability ({@code addStaticEffect}) and
 * handled during {@code BattlefieldEntryService.handleCreatureEnteredBattlefield} before ETB triggers
 * fire. The entering permanent itself is never offered — it isn't on the battlefield yet as far as
 * the ability is concerned. The payoff counts only what was actually sacrificed and reads the sacrificed creatures'
 * power and toughness as they last existed on the battlefield. Contrast {@link DevourEffect}, which is the Devour
 * keyword: it is limited to creatures and records the devoured cards on the permanent.
 */
public record SacrificePermanentsAsEntersForCountersEffect(PermanentPredicate filter,
                                                           int countersPerPermanent,
                                                           Payoff payoff)
        implements ReplacementEffect {

    /** What the entering permanent gets for the permanents sacrificed as it enters. */
    public enum Payoff {
        /** It enters with {@code countersPerPermanent} +1/+1 counters for each permanent sacrificed. */
        COUNTERS,
        /**
         * "Its power becomes the total power of those creatures and its toughness becomes their total toughness": a
         * base power and toughness setter (layer 7b) with the entry's timestamp, set even when nothing was sacrificed.
         */
        BASE_POWER_TOUGHNESS_TOTAL,
        /**
         * "Its power and toughness are each equal to the number sacrificed as it entered": the card's
         * characteristic-defining ability (CR 604.3, layer 7a), ordered before every layer 7b setter.
         */
        POWER_TOUGHNESS_EQUAL_TO_COUNT
    }

    public SacrificePermanentsAsEntersForCountersEffect(PermanentPredicate filter, int countersPerPermanent) {
        this(filter, countersPerPermanent, Payoff.COUNTERS);
    }

    /** "Sacrifice any number of creatures. This creature's power becomes the total power of those creatures ..." */
    public static SacrificePermanentsAsEntersForCountersEffect basePowerToughnessEqualToTotalOfCreatures() {
        return new SacrificePermanentsAsEntersForCountersEffect(new PermanentIsCreaturePredicate(), 0,
                Payoff.BASE_POWER_TOUGHNESS_TOTAL);
    }

    /** "Sacrifice any number of [filter]. Its power and toughness are each equal to the number sacrificed." */
    public static SacrificePermanentsAsEntersForCountersEffect powerToughnessEqualToNumberSacrificed(
            PermanentPredicate filter) {
        return new SacrificePermanentsAsEntersForCountersEffect(filter, 0, Payoff.POWER_TOUGHNESS_EQUAL_TO_COUNT);
    }
}
