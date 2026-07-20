package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Attack trigger that removes one counter of {@code counterType} from the creature chosen as the first
 * target (a creature the attacking player controls) and puts one counter of the same type on the
 * creature chosen as the second target (up to one creature the defending player controls). Decimator
 * Beetle: {@code MINUS_ONE_MINUS_ONE}.
 *
 * <p>The two halves are <em>independent</em> (CR-accurate): the counter is put on the defending
 * creature even if the controller's creature had no counter to remove, and if one target becomes
 * illegal the other still resolves. The put half fires the normal "-1/-1 counter put on a creature"
 * triggers and respects can't-have-counters, exactly like {@code PutCounterOnTargetPermanentEffect};
 * the remove half is a no-op when the source creature has no such counter.
 *
 * <p>Targets are read from the stack entry's flat {@code targetIds} list (0 = remove-from, 1 = put-on,
 * optional). Routed through the two-step attack pipeline via {@link AttackCounterMoveEffect}.
 */
public record RemoveAndPutCounterOnAttackEffect(CounterType counterType) implements AttackCounterMoveEffect {

    /** Decimator Beetle: move a -1/-1 counter. */
    public RemoveAndPutCounterOnAttackEffect() {
        this(CounterType.MINUS_ONE_MINUS_ONE);
    }
}
