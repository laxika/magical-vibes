package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * ETB replacement effect: "This creature enters the battlefield with a +1/+1 counter on it
 * for each other [subtype] you control and each [subtype] card in your graveyard."
 * (e.g. Unbreathing Horde)
 *
 * @param subtype          the subtype to count
 * @param includeGraveyard whether to also count cards of this subtype in the controller's graveyard
 */
public record EnterWithPlusOnePlusOneCountersPerSubtypeEffect(
        CardSubtype subtype,
        boolean includeGraveyard
) implements ReplacementEffect {
}
