package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "As this permanent enters, [if it was kicked,] mill {@code count} cards." Applied while the permanent
 * enters, without using the stack, so state-based actions never see it on the battlefield before the
 * cards are milled (CR 614.1c).
 *
 * @param count the number of cards the controller mills, evaluated as the permanent enters
 * @param onlyIfKicked whether the mill happens only when the entering permanent's kicker cost was paid
 */
public record MillAsEntersEffect(DynamicAmount count, boolean onlyIfKicked) implements ReplacementEffect {

    public MillAsEntersEffect(int count, boolean onlyIfKicked) {
        this(new Fixed(count), onlyIfKicked);
    }
}
