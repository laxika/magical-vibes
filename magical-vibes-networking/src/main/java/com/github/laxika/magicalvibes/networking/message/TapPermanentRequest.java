package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.ManaPaymentIntent;

/**
 * @param paymentIntent what the player is tapping this source for, when the tap serves a held-back
 *                      cast or activation; {@code null} otherwise. Advisory only — see
 *                      {@link ManaPaymentIntent}.
 */
public record TapPermanentRequest(int permanentIndex, ManaPaymentIntent paymentIntent) {

    public TapPermanentRequest(int permanentIndex) {
        this(permanentIndex, null);
    }
}
