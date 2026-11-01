package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;
import java.util.UUID;

/**
 * A card or permanent moved from one zone to another (CR 400.7).
 *
 * <p>A permanent leaving the battlefield carries its last-known information: {@code permanent} is a copy
 * taken just before it left, with its effective card, last-known power and toughness and whether it had
 * lost its abilities, so leaves-the-battlefield abilities can look back in time (CR 603.10a). A departure
 * of a simultaneous batch carries the snapshot {@code DepartureService} took before any of the batch left.
 * A permanent entering the battlefield carries a copy taken as it entered. Moves between other zones carry
 * no permanent.
 *
 * @param card         the physical card that moved; for a merged or melded permanent, its original card
 * @param ownerId      the card's owner, or {@code null} when unknown (hand-built test cards)
 * @param from         the zone it left, or {@code null} when it came from outside the game or nothing knew
 *                     where it was (a created token, a conjured card)
 * @param to           the zone it went to; a token that left the battlefield goes to the zone it was put in,
 *                     before it ceases to exist (CR 111.7)
 * @param cause        how it moved
 * @param permanent    the battlefield snapshot described above, or {@code null}
 * @param controllerId the permanent's controller as it left or entered the battlefield, or {@code null}
 */
public record ZoneChangeEvent(Card card, UUID ownerId, Zone from, Zone to, ZoneChangeCause cause,
                              Permanent permanent, UUID controllerId) implements RulesEvent {

    public ZoneChangeEvent {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(cause, "cause");
    }

    /** Whether a permanent left the battlefield (CR 603.6c). */
    public boolean leftBattlefield() {
        return from == Zone.BATTLEFIELD;
    }

    /** Whether a permanent entered the battlefield (CR 603.6a). */
    public boolean enteredBattlefield() {
        return to == Zone.BATTLEFIELD;
    }

    /** This event with another destination, for a move that a replacement effect sent elsewhere. */
    public ZoneChangeEvent withTo(Zone destination) {
        return new ZoneChangeEvent(card, ownerId, from, destination, cause, permanent, controllerId);
    }

    @Override
    public ZoneChangeEvent deepCopy() {
        return permanent == null ? this
                : new ZoneChangeEvent(card, ownerId, from, to, cause, new Permanent(permanent), controllerId);
    }
}
