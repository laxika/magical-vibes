package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static ability that functions while the source permanent is on the battlefield:
 * each other creature with the specified {@code subtype} controlled by the source's
 * controller enters the battlefield with additional +1/+1 counters.
 * <p>
 * This is a replacement effect (MTG Rule 614.1c) — it modifies how the creature
 * enters the battlefield rather than triggering after the fact. "Other" is handled
 * implicitly: the source is already on the battlefield while the affected creature is
 * still entering, and a creature entering simultaneously with the source does not see
 * the source's replacement effect (CR 614.12).
 * <p>
 * Used by cards like Sage of Fables ("Each other Wizard creature you control enters
 * with an additional +1/+1 counter on it.").
 */
public record ControlledCreaturesEnterWithAdditionalCountersEffect(
        CardSubtype subtype,
        int count
) implements CardEffect {
}
