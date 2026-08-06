package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Move a counter from target creature onto a second target creature." (e.g. Leech Bonder's
 * {@code {U}, {Q}} ability), or "Move all counters from target creature onto another target
 * creature." when {@code moveAll} is {@code true} (e.g. Fate Transfer).
 *
 * <p>Reads two targets from the flat multi-target list: position 0 is the creature counters are
 * removed from, position 1 is the creature they are placed on. When {@code moveAll} is {@code false},
 * a single counter of one kind is moved (the first present kind if several exist); when {@code true},
 * every counter of every kind is moved. If the first creature has no counters (or either target is
 * gone) when the effect resolves, nothing happens.</p>
 *
 * <p>{@code anyNumber} switches to the "move any number of {@code counterType} counters" shape
 * (Bioshift): on resolution the controller is prompted for how many of that one counter kind
 * (0..the count present) to move. {@code counterType} is required in that mode and ignored
 * otherwise.</p>
 */
public record MoveCounterFromTargetCreatureToTargetCreatureEffect(boolean moveAll, CounterType counterType,
                                                                  boolean anyNumber) implements CardEffect {

    /** Convenience constructor for the single-counter variant (Leech Bonder). */
    public MoveCounterFromTargetCreatureToTargetCreatureEffect() {
        this(false);
    }

    /** "Move a counter" / "move all counters" without a counter-kind restriction. */
    public MoveCounterFromTargetCreatureToTargetCreatureEffect(boolean moveAll) {
        this(moveAll, null, false);
    }

    /** "Move any number of {@code counterType} counters" (Bioshift). */
    public MoveCounterFromTargetCreatureToTargetCreatureEffect(CounterType counterType) {
        this(false, counterType, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
