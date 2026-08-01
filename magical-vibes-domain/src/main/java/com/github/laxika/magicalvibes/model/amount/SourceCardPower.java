package com.github.laxika.magicalvibes.model.amount;

/**
 * The power of the source <em>card</em> (not permanent), never negative. Unlike
 * {@link SourcePower}, which reads the source permanent's effective power on the battlefield, this
 * reads the printed power of the card behind the spell or ability — the value scavenge needs
 * (CR 702.97a: "Put a number of +1/+1 counters equal to the power of the card you exiled on target
 * creature"), because the scavenged card is exiled as an activation cost and never was a permanent.
 * Evaluates to 0 when there is no source card or it has no power (a noncreature card).
 */
public record SourceCardPower() implements DynamicAmount {
}
