package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.StepKind;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * "That many", "that much", "equal to the number of cards [drawn | milled | discarded] this way", "equal to the damage
 * dealt this way": the amount the most recent {@code kind} step of the resolving entry recorded on its resolution
 * scratchpad ({@code StackEntry.getResolutionResults().outcome(kind).amount()}; what each kind counts is listed on
 * {@link StepKind}). 0 when no such step ran or it did nothing, and outside a resolution. The answer is read once, as
 * the reading effect is applied (CR 608.2h).
 *
 * <p>With a {@link Stat} other than {@link Stat#AMOUNT} it is "the sacrificed creature's power", "that creature's
 * toughness", "its mana value": the total of that stat over the permanents the most recent {@link StepKind#SACRIFICE}
 * step sacrificed, as each last existed on the battlefield (CR 608.2h, its {@code DepartureResult}), each counted as at
 * least 0; a permanent that wasn't a creature has no power or toughness and counts 0. Only a sacrifice step records
 * those for every way it sacrifices, so a stat is read from one, or from an {@link StepKind#EXILE} step: "the
 * exiled card's toughness", "that card's mana value" is the total over what it exiled, each as it last existed in the
 * zone it left (CR 608.2h): a card exiled from a graveyard as the step recorded it there (a power that counts the
 * graveyards changes as the card leaves; a reflexive "when you do" ability reads it as it is in exile, where that
 * ability expects it), an exiled permanent as it last existed on the battlefield, a card exiled from
 * a library or a hand as it is now in exile. A stat's total is at least 0 (CR 107.1b), unless {@code negativeKept}
 * ({@link #keepingNegative()}): an effect that sets a creature's power or toughness to it uses a negative value as it
 * is ("create a token whose power is equal to that card's power", Ritual of the Returned, Soul Separator).
 *
 * <p>{@link Stat#DIED}, {@code filter} and {@code yours} count only some of what a removal step that records its
 * departures removed (a sacrifice step, or the destroy step of {@code DestroyAllPermanentsEffect} or
 * {@code DestroyEachTargetPermanentEffect}): "for each creature that died this way" counts those put into a graveyard
 * (CR 700.4: one a replacement exiled instead was destroyed but didn't die), "for each nontoken creature destroyed this
 * way" those that, as they last existed on the battlefield, match {@code filter}, and "the number of creatures you
 * controlled that were destroyed this way" those the amount's controller controlled as they left (CR 608.2h). The
 * amount's controller is the effect's controller, or the player an effect evaluates its amount for ("each player loses
 * life equal to the number of creatures they controlled that were destroyed this way", Deadly Tempest; "for each
 * creature destroyed this way, its controller creates a token"). Such a destroy step also records the last-known
 * power, toughness and mana value of what it destroyed ("the total mana value of permanents destroyed this way",
 * Convert to Slime).
 *
 * <p>{@code includesUnremoved} ({@link #includingUnremoved()}) widens a count over a destroy step that records its
 * departures to every permanent its instruction applied to, those that weren't destroyed (indestructible, regenerated,
 * a shield counter) included: "for each of those permanents, its controller creates …" (Soul of Emancipation). A target
 * that was illegal as the step resolved was never applied to and doesn't count.
 *
 * @param kind               the kind of step read
 * @param stat               what is read from it
 * @param filter             only the removed permanents that match it count; {@code null} for all of them
 * @param yours              only the removed permanents the amount's controller controlled count
 * @param includesUnremoved  every permanent the destroy step applied to counts, not only those it destroyed
 * @param negativeKept       a negative power or toughness total is the amount, for an effect that sets a power or
 *                           toughness to it
 */
public record AmountThisWay(StepKind kind, Stat stat, PermanentPredicate filter, boolean yours,
                            boolean includesUnremoved, boolean negativeKept)
        implements DynamicAmount {

    /** What {@link AmountThisWay} reads from the step. */
    public enum Stat {
        /** The step's amount ({@code StepOutcome.amount()}), or how many of what it removed count. */
        AMOUNT,
        /** How many of the permanents the step removed died: were put into a graveyard from the battlefield. */
        DIED,
        /** The last-known power of what the step sacrificed, destroyed or exiled. */
        POWER,
        /** The last-known toughness of what the step sacrificed, destroyed or exiled. */
        TOUGHNESS,
        /** The last-known mana value of what the step sacrificed, destroyed or exiled. */
        MANA_VALUE
    }

    public AmountThisWay {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(stat, "stat");
        boolean removal = kind == StepKind.SACRIFICE || kind == StepKind.DESTROY;
        if ((stat == Stat.POWER || stat == Stat.TOUGHNESS || stat == Stat.MANA_VALUE)
                && !removal && kind != StepKind.EXILE) {
            throw new IllegalArgumentException(
                    "Only a sacrifice, destroy or exile step records last-known " + stat + ": " + kind);
        }
        if ((stat == Stat.DIED || filter != null || yours) && !removal) {
            throw new IllegalArgumentException(
                    "Only a sacrifice or destroy step records whether and whose what it removed died: " + kind);
        }
        if (includesUnremoved && (kind != StepKind.DESTROY || stat == Stat.DIED)) {
            throw new IllegalArgumentException(
                    "Only a destroy step's count or stat covers what it didn't destroy: " + kind + " " + stat);
        }
        if (negativeKept && stat != Stat.POWER && stat != Stat.TOUGHNESS) {
            throw new IllegalArgumentException("Only a power or toughness can be negative: " + stat);
        }
    }

    /** The amount over the removed permanents that count, a negative total read as 0. */
    public AmountThisWay(StepKind kind, Stat stat, PermanentPredicate filter, boolean yours,
                         boolean includesUnremoved) {
        this(kind, stat, filter, yours, includesUnremoved, false);
    }

    /** The amount over only the removed permanents matching {@code filter}, or those the amount's controller controlled. */
    public AmountThisWay(StepKind kind, Stat stat, PermanentPredicate filter, boolean yours) {
        this(kind, stat, filter, yours, false);
    }

    /** The {@code stat} of everything the most recent {@code kind} step recorded. */
    public AmountThisWay(StepKind kind, Stat stat) {
        this(kind, stat, null, false);
    }

    /** The amount the most recent {@code kind} step recorded. */
    public AmountThisWay(StepKind kind) {
        this(kind, Stat.AMOUNT);
    }

    /** "For each creature that died this way": how many of what the most recent removal step removed died. */
    public static AmountThisWay died(StepKind kind) {
        return new AmountThisWay(kind, Stat.DIED);
    }

    /** "For each nontoken creature destroyed this way": this amount over only the removed permanents matching it. */
    public AmountThisWay matching(PermanentPredicate filter) {
        return new AmountThisWay(kind, stat, Objects.requireNonNull(filter, "filter"), yours, includesUnremoved,
                negativeKept);
    }

    /**
     * "The number of creatures you controlled that were destroyed this way": this amount over only the removed
     * permanents the amount's controller controlled.
     */
    public AmountThisWay controlledByYou() {
        return new AmountThisWay(kind, stat, filter, true, includesUnremoved, negativeKept);
    }

    /**
     * "For each of those permanents" after a destroy step: this amount over every permanent the step's instruction
     * applied to, those it didn't destroy included.
     */
    public AmountThisWay includingUnremoved() {
        return new AmountThisWay(kind, stat, filter, yours, true, negativeKept);
    }

    /**
     * "Create a token whose power is equal to that card's power": this power or toughness total as it is, a negative
     * one included, for an effect that sets a creature's power or toughness to it (CR 107.1b).
     */
    public AmountThisWay keepingNegative() {
        return new AmountThisWay(kind, stat, filter, yours, includesUnremoved, true);
    }

    /** "The total mana value of permanents destroyed this way": the last-known mana value of what a destroy step destroyed. */
    public static AmountThisWay destroyedManaValue() {
        return new AmountThisWay(StepKind.DESTROY, Stat.MANA_VALUE);
    }

    /** "The sacrificed creature's power": the last-known power of what the most recent sacrifice step sacrificed. */
    public static AmountThisWay sacrificedPower() {
        return new AmountThisWay(StepKind.SACRIFICE, Stat.POWER);
    }

    /** "That creature's toughness" after a sacrifice step. */
    public static AmountThisWay sacrificedToughness() {
        return new AmountThisWay(StepKind.SACRIFICE, Stat.TOUGHNESS);
    }

    /** "Its mana value" after a sacrifice step. */
    public static AmountThisWay sacrificedManaValue() {
        return new AmountThisWay(StepKind.SACRIFICE, Stat.MANA_VALUE);
    }

    /** "The exiled creature card's power" after an exile step. */
    public static AmountThisWay exiledPower() {
        return new AmountThisWay(StepKind.EXILE, Stat.POWER);
    }

    /** "The exiled card's toughness" after an exile step. */
    public static AmountThisWay exiledToughness() {
        return new AmountThisWay(StepKind.EXILE, Stat.TOUGHNESS);
    }

    /** "The exiled card's mana value", "that card's mana value" after an exile step. */
    public static AmountThisWay exiledManaValue() {
        return new AmountThisWay(StepKind.EXILE, Stat.MANA_VALUE);
    }
}
