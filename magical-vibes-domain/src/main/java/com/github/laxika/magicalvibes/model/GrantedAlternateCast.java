package com.github.laxika.magicalvibes.model;

import java.util.Objects;

/**
 * An alternative cost a card has without having it printed (CR 118.9): "you may pay {R} rather than pay this spell's
 * mana cost" ({@code evoke} false), or evoke (CR 702.74a), whose triggered half, "when this permanent enters, if its
 * evoke cost was paid, its controller sacrifices it", the cast adds to the spell ({@code evoke} true). A perpetual one
 * is kept in {@code GameData.perpetualAlternateCasts}; a permanent's evoke grant to the cards of its controller is
 * found as it applies.
 *
 * @param cast  what is paid rather than the mana cost
 * @param evoke whether it is an evoke cost
 */
public record GrantedAlternateCast(AlternateHandCast cast, boolean evoke) {

    public GrantedAlternateCast {
        Objects.requireNonNull(cast, "cast");
    }
}
