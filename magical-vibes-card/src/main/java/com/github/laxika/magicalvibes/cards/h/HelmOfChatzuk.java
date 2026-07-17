package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Helm of Chatzuk — {1} Artifact.
 * "{1}, {T}: Target creature gains banding until end of turn."
 * Banding is not modeled by the engine, so the ability is intentionally omitted.
 * No card-specific engine logic.
 */
@CardRegistration(set = "5ED", collectorNumber = "376")
public class HelmOfChatzuk extends Card {

    public HelmOfChatzuk() {
    }
}
