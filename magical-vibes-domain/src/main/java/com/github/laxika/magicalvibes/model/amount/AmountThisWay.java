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
 * exiled card's toughness", "that card's mana value" is the total over what it exiled, a card exiled from a
 * graveyard, a library or a hand as it is now (a card keeps its characteristics in exile, CR 608.2h), an exiled
 * permanent as it last existed on the battlefield.
 *
 * <p>{@link Stat#DIED}, {@code filter} and {@code yours} count only some of what a removal step that records its
 * departures removed (a sacrifice step, or the destroy step of {@code DestroyAllPermanentsEffect}): "for each creature
 * that died this way" counts those put into a graveyard (CR 700.4: one a replacement exiled instead was destroyed but
 * didn't die), "for each nontoken creature destroyed this way" those that, as they last existed on the battlefield,
 * match {@code filter}, and "the number of creatures you controlled that were destroyed this way" those the amount's
 * controller controlled as they left (CR 608.2h). The amount's controller is the effect's controller, or the player an
 * effect evaluates its amount for ("each player loses life equal to the number of creatures they controlled that were
 * destroyed this way", Deadly Tempest; "for each creature destroyed this way, its controller creates a token").
 *
 * @param kind   the kind of step read
 * @param stat   what is read from it
 * @param filter only the removed permanents that match it count; {@code null} for all of them
 * @param yours  only the removed permanents the amount's controller controlled count
 */
public record AmountThisWay(StepKind kind, Stat stat, PermanentPredicate filter, boolean yours)
        implements DynamicAmount {

    /** What {@link AmountThisWay} reads from the step. */
    public enum Stat {
        /** The step's amount ({@code StepOutcome.amount()}), or how many of what it removed count. */
        AMOUNT,
        /** How many of the permanents the step removed died: were put into a graveyard from the battlefield. */
        DIED,
        /** The last-known power of what the step sacrificed or exiled. */
        POWER,
        /** The last-known toughness of what the step sacrificed or exiled. */
        TOUGHNESS,
        /** The last-known mana value of what the step sacrificed or exiled. */
        MANA_VALUE
    }

    public AmountThisWay {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(stat, "stat");
        boolean removal = kind == StepKind.SACRIFICE || kind == StepKind.DESTROY;
        if ((stat == Stat.POWER || stat == Stat.TOUGHNESS || stat == Stat.MANA_VALUE)
                && kind != StepKind.SACRIFICE && kind != StepKind.EXILE) {
            throw new IllegalArgumentException(
                    "Only a sacrifice or exile step records last-known " + stat + ": " + kind);
        }
        if ((stat == Stat.DIED || filter != null || yours) && !removal) {
            throw new IllegalArgumentException(
                    "Only a sacrifice or destroy step records whether and whose what it removed died: " + kind);
        }
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
        return new AmountThisWay(kind, stat, Objects.requireNonNull(filter, "filter"), yours);
    }

    /**
     * "The number of creatures you controlled that were destroyed this way": this amount over only the removed
     * permanents the amount's controller controlled.
     */
    public AmountThisWay controlledByYou() {
        return new AmountThisWay(kind, stat, filter, true);
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
