package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Manifests the top {@code amount} cards of the effect controller's library, one at a time (CR 701.40e). The amount is
 * evaluated as the effect resolves: "manifest a number of cards equal to the number of counters on it" is
 * {@code new ManifestTopCardEffect(new CountersOnTriggeringPermanent())} (Omarthis, Ghostfire Initiate).
 *
 * @param amount how many cards to manifest
 */
public record ManifestTopCardEffect(DynamicAmount amount) implements CardEffect {

    /** Manifests the top card. */
    public ManifestTopCardEffect() {
        this(new Fixed(1));
    }
}
