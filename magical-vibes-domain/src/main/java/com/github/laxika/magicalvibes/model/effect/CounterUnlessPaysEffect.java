package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter target spell unless its controller pays a mana amount.
 *
 * @param amount            the generic mana amount to pay (ignored when {@code useXValue} is true,
 *                          except in the PendingMayAbility where it carries the resolved X)
 * @param useXValue         if true, read the amount from the stack entry's X value instead of {@code amount}
 * @param exileIfCountered  if true, exile the countered spell instead of putting it into the graveyard
 */
public record CounterUnlessPaysEffect(int amount, boolean useXValue, boolean exileIfCountered) implements CardEffect {

    /** Fixed-amount counter-unless-pays (e.g. Mana Leak). */
    public CounterUnlessPaysEffect(int amount) {
        this(amount, false, false);
    }

    @Override
    public boolean canTargetSpell() {
        return true;
    }
}
