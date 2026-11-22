package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * "This creature enters with X +1/+1 counters on it, where X is …" of a cast trigger: the spell the trigger was about
 * has X equal to {@code amount} from then on (Naya Soulbeast). Nothing happens once that spell has left the stack.
 */
public record SetTriggeringSpellXValueEffect(DynamicAmount amount) implements CardEffect {

    public SetTriggeringSpellXValueEffect {
        Objects.requireNonNull(amount, "amount");
    }
}
