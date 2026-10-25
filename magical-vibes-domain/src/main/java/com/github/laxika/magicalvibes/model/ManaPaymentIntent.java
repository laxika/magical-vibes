package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * What a player is tapping mana for, declared by the client on the mana activation itself.
 *
 * <p>The MTGO-style "click the card, then tap your sources" flow lives entirely in the client: it
 * holds the cast or activation back until the pool covers the cost, so the server never sees a
 * half-paid spell and has no payment session of its own. This record supplies that missing context,
 * and it is <em>advisory only</em> — the engine uses it solely to grey out mana colours that would
 * strand the payment, and answering with a greyed-out colour is still legal.
 *
 * <p>At most one of the two shapes is populated. A hand cast carries {@code handCardIndex} (plus the
 * announced {@code xValue}); an activated-ability payment carries {@code abilityPermanentId} and
 * {@code abilityIndex}. The cost itself is deliberately not carried — the engine recomputes it from
 * the card or ability so cost modifiers stay server-authoritative. An {@linkplain #unspecified()
 * unspecified} intent populates neither: the tap serves a payment the engine should not narrow
 * colours for (a kicked cast, the AI paying for its next action).
 *
 * <p>Beyond colour narrowing, the presence of an intent is what marks a mana activation as part of a
 * payment in progress: its non-mana triggers are held until the spell or ability is put on the stack
 * (or dropped if the payment is cancelled), whereas a mana ability activated on its own with no
 * intent puts its triggers on the stack straight away (CR 603.3, CR 117.3c).
 *
 * <p>Every component is boxed on purpose: the record arrives straight off the wire with only the
 * fields its shape uses, and a primitive would fail to bind whenever the other shape's fields are
 * absent rather than defaulting.
 *
 * @param handCardIndex      hand index of the spell being paid for, or {@code null}
 * @param xValue             generic mana announced for {X}; {@code null} or 0 when there is no {X}
 * @param abilityPermanentId source of the activated ability being paid for, or {@code null}
 * @param abilityIndex       index into that permanent's activated abilities, or {@code null}
 */
public record ManaPaymentIntent(Integer handCardIndex, Integer xValue,
                                UUID abilityPermanentId, Integer abilityIndex) {

    public static ManaPaymentIntent forCast(int handCardIndex, int xValue) {
        return new ManaPaymentIntent(handCardIndex, xValue, null, null);
    }

    public static ManaPaymentIntent forAbility(UUID permanentId, int abilityIndex) {
        return new ManaPaymentIntent(null, null, permanentId, abilityIndex);
    }

    /** A payment in progress whose cost the engine should not try to narrow colours for. */
    public static ManaPaymentIntent unspecified() {
        return new ManaPaymentIntent(null, null, null, null);
    }

    /** The announced {X}, treating an absent value as 0. */
    public int announcedX() {
        return xValue == null ? 0 : Math.max(0, xValue);
    }

    public boolean isCast() {
        return handCardIndex != null;
    }

    public boolean isAbility() {
        return abilityPermanentId != null && abilityIndex != null;
    }
}
