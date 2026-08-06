package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static ability that functions while the source permanent is on the battlefield: each other
 * creature its controller controls enters with a number of additional +1/+1 counters on it equal
 * to the source's power, and with {@code addedSubtype} in addition to its other types.
 * <p>
 * This is a replacement effect (MTG Rule 614.1c) — it modifies how the creature enters rather than
 * triggering afterwards. "Other" is implicit: the source is already on the battlefield while the
 * affected creature is still entering, and a creature entering simultaneously with the source does
 * not see the source's replacement effect (CR 614.12). The granted subtype is stamped onto the
 * entering permanent, so it persists even after the source leaves the battlefield.
 * <p>
 * Used by Master Biomancer.
 *
 * @param addedSubtype subtype the entering creature gains in addition to its other types
 */
public record ControlledCreaturesEnterWithSourcePowerCountersEffect(
        CardSubtype addedSubtype
) implements CardEffect {
}
