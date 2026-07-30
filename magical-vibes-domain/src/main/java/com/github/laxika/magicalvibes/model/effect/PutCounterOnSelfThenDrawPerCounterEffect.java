package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Put a counter of {@code counterType} on this permanent, then have its controller draw a card for
 * each counter of that type on it ("put a lore counter on this enchantment, then draw a card for
 * each lore counter on this enchantment" — Mind Unbound).
 *
 * <p>One effect rather than a counter effect plus a draw effect because both halves belong to the
 * same triggered ability: separate effects would become separate stack entries, and the LIFO stack
 * would resolve the draw before the counter was placed.
 */
public record PutCounterOnSelfThenDrawPerCounterEffect(CounterType counterType) implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new CountersOnSource(counterType);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
