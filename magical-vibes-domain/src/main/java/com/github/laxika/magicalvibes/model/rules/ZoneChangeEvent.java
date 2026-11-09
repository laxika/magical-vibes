package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
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
 * @param grantedAbilities for a departure, the triggered abilities other objects' static abilities granted the
 *                     permanent just before it left (CR 603.10a), which nothing on the permanent itself records;
 *                     empty otherwise
 * @param deathTriggersSuppressed for a departure, whether a static ability such as Hushbringer's kept a creature
 *                     dying there from causing abilities to trigger, as it was just before it left; it matters
 *                     only if the move is a death
 * @param castingSpell for a departure, the spell its controller was casting as it left (CR 601.2): sacrificed to pay
 *                     that spell's costs or a mana ability's activated to pay for it ("when you sacrifice this while
 *                     casting a spell with emerge"), or {@code null}
 * @param attachments  for a departure, the Auras and Equipment attached to the permanent just before it left;
 *                     {@link DepartedAttachments#NONE} otherwise
 * @param hauntedBy    for a departure, the cards in exile that haunted the permanent (CR 702.55b) just before it
 *                     left, whose "when the creature it haunts dies" abilities it can trigger; empty otherwise
 * @param causeControllerId the controller of the spell or ability that was resolving as it moved, or {@code null}
 *                     when none was: a cost, a state-based action, a turn-based action
 */
public record ZoneChangeEvent(Card card, UUID ownerId, Zone from, Zone to, ZoneChangeCause cause,
                              Permanent permanent, UUID controllerId, List<Ability> grantedAbilities,
                              boolean deathTriggersSuppressed, Card castingSpell,
                              DepartedAttachments attachments, List<UUID> hauntedBy, UUID causeControllerId)
        implements RulesEvent {

    public ZoneChangeEvent {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(cause, "cause");
        grantedAbilities = List.copyOf(grantedAbilities);
        Objects.requireNonNull(attachments, "attachments");
        hauntedBy = List.copyOf(hauntedBy);
    }

    /** This zone change, with {@code permanent} as the copy of the permanent that moved. */
    public ZoneChangeEvent withPermanent(Permanent permanent) {
        return new ZoneChangeEvent(card, ownerId, from, to, cause, permanent, controllerId, grantedAbilities,
                deathTriggersSuppressed, castingSpell, attachments, hauntedBy, causeControllerId);
    }

    /** A zone change with no granted abilities. */
    public ZoneChangeEvent(Card card, UUID ownerId, Zone from, Zone to, ZoneChangeCause cause,
                           Permanent permanent, UUID controllerId) {
        this(card, ownerId, from, to, cause, permanent, controllerId, List.of(), false, null, DepartedAttachments.NONE,
                List.of(), null);
    }

    /** A zone change with the given departure look-back and no resolving cause. */
    public ZoneChangeEvent(Card card, UUID ownerId, Zone from, Zone to, ZoneChangeCause cause,
                           Permanent permanent, UUID controllerId, List<Ability> grantedAbilities,
                           boolean deathTriggersSuppressed, Card castingSpell,
                           DepartedAttachments attachments, List<UUID> hauntedBy) {
        this(card, ownerId, from, to, cause, permanent, controllerId, grantedAbilities, deathTriggersSuppressed,
                castingSpell, attachments, hauntedBy, null);
    }

    /** This event with the controller of the spell or ability that was resolving as it happened. */
    public ZoneChangeEvent withCauseController(UUID resolvingControllerId) {
        return new ZoneChangeEvent(card, ownerId, from, to, cause, permanent, controllerId, grantedAbilities,
                deathTriggersSuppressed, castingSpell, attachments, hauntedBy, resolvingControllerId);
    }

    /** Whether a permanent left the battlefield (CR 603.6c). */
    public boolean leftBattlefield() {
        return from == Zone.BATTLEFIELD;
    }

    /** Whether a permanent died: was put into a graveyard from the battlefield (CR 700.4). */
    public boolean died() {
        return from == Zone.BATTLEFIELD && to == Zone.GRAVEYARD;
    }

    /** Whether a permanent entered the battlefield (CR 603.6a). */
    public boolean enteredBattlefield() {
        return to == Zone.BATTLEFIELD;
    }

    /** This event with another destination, for a move that a replacement effect sent elsewhere. */
    public ZoneChangeEvent withTo(Zone destination) {
        return new ZoneChangeEvent(card, ownerId, from, destination, cause, permanent, controllerId, grantedAbilities,
                deathTriggersSuppressed, castingSpell, attachments, hauntedBy, causeControllerId);
    }

    @Override
    public ZoneChangeEvent deepCopy() {
        return permanent == null ? this
                : new ZoneChangeEvent(card, ownerId, from, to, cause, new Permanent(permanent), controllerId,
                        grantedAbilities, deathTriggersSuppressed, castingSpell, attachments, hauntedBy,
                        causeControllerId);
    }
}
