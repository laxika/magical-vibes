package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The counter move: "move a [kind] counter from this permanent onto target creature" (Afiya Grove), "move X counters"
 * (Tester of the Tangential, whose reflexive ability reads the X paid), "move a counter from target creature onto this
 * creature" ({@link #fromTargetOntoSource}), "move a counter from target creature onto a second target creature"
 * ({@link #move} over {@code target(0)} and {@code target(1)}), "move all counters" ({@link #all}) and "move any number
 * of counters" ({@link #anyNumber}), also from or onto a set ("from among creatures you control", "onto other
 * creatures").
 *
 * <p>Moving is a removal plus a placement (CR 122.5): nothing is removed or put when either permanent is gone, both
 * are one permanent, the first has no such counter, or the second can't have it put on it. The counters are put by
 * the ability's controller, so counter-placement replacements judge the mover as their placer.</p>
 *
 * <ul>
 *   <li>{@link Quantity#AMOUNT}: {@code amount} counters of {@code counterType}, or fewer when fewer are there. With
 *   {@link CounterType#ANY} it is one counter of a kind the controller chooses as the effect resolves, unasked when
 *   only one kind can move (CR 608.2d).</li>
 *   <li>{@link Quantity#ALL}: every counter of {@code counterType}, every kind for {@link CounterType#ANY}.</li>
 *   <li>{@link Quantity#ANY_NUMBER}: for each permanent {@code from} names, each permanent {@code to} names and each
 *   kind ({@code counterType}, or every kind on it for {@link CounterType#ANY}), the controller chooses how many to
 *   move (zero included); the counters are put on each destination together once every choice is made.</li>
 * </ul>
 *
 * @param counterType   the kind of counter moved, {@link CounterType#ANY} for any kind
 * @param amount        the number of counters to move for {@link Quantity#AMOUNT}, evaluated as the effect resolves
 * @param anotherTarget whether the target is "another target creature", never the source itself
 * @param from          whose counters move
 * @param to            where they are put
 * @param anyPermanent  whether the target is any permanent rather than a creature (the ability's filter narrows it)
 * @param quantity      how many counters move
 */
public record MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount,
                                                          boolean anotherTarget, PermanentSubject from,
                                                          PermanentSubject to, boolean anyPermanent,
                                                          Quantity quantity)
        implements CardEffect {

    /** How many counters a move moves. */
    public enum Quantity { AMOUNT, ALL, ANY_NUMBER }

    public MoveCounterFromSourceToTargetCreatureEffect {
        if (from == null || to == null || quantity == null || counterType == null) {
            throw new IllegalArgumentException("A counter move needs a kind, both subjects and a quantity");
        }
        if (anotherTarget && anyPermanent) {
            throw new IllegalArgumentException("\"Another target\" is a creature here");
        }
        if (quantity == Quantity.AMOUNT && counterType == CounterType.ANY
                && !(amount instanceof Fixed fixed && fixed.value() == 1)) {
            throw new IllegalArgumentException("A move of a chosen kind moves one counter");
        }
        if (quantity == Quantity.AMOUNT && amount == null) {
            throw new IllegalArgumentException("A move of an amount needs the amount");
        }
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount,
                                                       boolean anotherTarget, PermanentSubject from,
                                                       PermanentSubject to, boolean anyPermanent) {
        this(counterType, amount, anotherTarget, from, to, anyPermanent, Quantity.AMOUNT);
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount,
                                                       boolean anotherTarget) {
        this(counterType, amount, anotherTarget, Subjects.source(), Subjects.target(), false);
    }

    /** "Move a [type] counter from target creature onto this creature" (Arcbound Fiend). */
    public static MoveCounterFromSourceToTargetCreatureEffect fromTargetOntoSource(CounterType counterType) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, new Fixed(1), false,
                Subjects.target(), Subjects.source(), false);
    }

    /** "Move [amount] counters from this permanent onto target [permanent]" (Blaster, Morale Booster). */
    public static MoveCounterFromSourceToTargetCreatureEffect ontoTargetPermanent(CounterType counterType,
                                                                                 DynamicAmount amount) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, amount, false,
                Subjects.source(), Subjects.target(), true);
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount) {
        this(counterType, amount, false);
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType) {
        this(counterType, new Fixed(1));
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, int amount) {
        this(counterType, new Fixed(amount));
    }

    /** "Move [amount] counters from this creature onto another target creature" (Tester of the Tangential). */
    public static MoveCounterFromSourceToTargetCreatureEffect ontoAnotherCreature(CounterType counterType,
                                                                                 DynamicAmount amount) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, amount, true);
    }

    /**
     * "Move a [kind] counter from [from] onto [to]"; {@link CounterType#ANY} for "a counter" of the controller's
     * choice ("from target creature onto a second target creature", Leech Bonder; "from this artifact onto that
     * creature", Agent's Toolkit).
     */
    public static MoveCounterFromSourceToTargetCreatureEffect move(CounterType counterType, PermanentSubject from,
                                                                  PermanentSubject to) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, new Fixed(1), false, from, to, false,
                Quantity.AMOUNT);
    }

    /** "Move all counters from [from] onto [to]" (Fate Transfer). */
    public static MoveCounterFromSourceToTargetCreatureEffect all(PermanentSubject from, PermanentSubject to) {
        return new MoveCounterFromSourceToTargetCreatureEffect(CounterType.ANY, null, false, from, to, false,
                Quantity.ALL);
    }

    /**
     * "Move any number of [kind] counters from [from] onto [to]" ({@link CounterType#ANY}: of any kinds): Bioshift,
     * Scrounging Bandar, Resourceful Defense, Slippery Bogbonder ("from among creatures you control"), Aetherborn
     * Marauder ("from other permanents you control"), Forgotten Ancient ("onto other creatures").
     */
    public static MoveCounterFromSourceToTargetCreatureEffect anyNumber(CounterType counterType,
                                                                       PermanentSubject from, PermanentSubject to) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, null, false, from, to, false,
                Quantity.ANY_NUMBER);
    }

    @Override
    public TargetSpec targetSpec() {
        boolean plainTarget = isPlainTarget(from) || isPlainTarget(to);
        if (plainTarget) {
            if (anotherTarget) {
                return TargetSpec.benign(TargetPredicates.creature(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
            }
            return TargetSpec.benign(anyPermanent ? TargetPredicates.permanent() : TargetPredicates.creature());
        }
        if (from.declaresTarget() || to.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        return TargetSpec.NONE;
    }

    private static boolean isPlainTarget(PermanentSubject subject) {
        return subject instanceof PermanentSubject.Target target && target.group() == null;
    }
}
