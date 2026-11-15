package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;

import java.util.UUID;

/**
 * A triggered ability that a rules event may trigger, with its source as the event sees it.
 *
 * @param sourceCard      the card whose ability it is; for a permanent that left the battlefield, the effective card
 *                        it left as
 * @param sourcePermanent the permanent whose ability it is (for a departure, its last-known copy), or {@code null}
 *                        for a card in another zone
 * @param controllerId    who controls the ability: the permanent's controller, the card's owner, or the planar
 *                        controller for a face-up plane's (CR 901.6)
 * @param zone            the zone the source is in for the ability, which decides whether it functions (CR 113.6)
 * @param ability         the ability
 * @param plane           the face-up plane or phenomenon whose ability it is, whose abilities function from the
 *                        command zone (CR 901.7), or {@code null}
 * @param emblem          the emblem whose ability it is, whose abilities function from the command zone (CR 114.4),
 *                        or {@code null}
 */
public record TriggerCandidate(Card sourceCard, Permanent sourcePermanent, UUID controllerId, Zone zone,
                               TriggeredAbility ability, PlanarObject plane, Emblem emblem) {

    /** A candidate whose source is neither a planar object nor an emblem. */
    public TriggerCandidate(Card sourceCard, Permanent sourcePermanent, UUID controllerId, Zone zone,
                            TriggeredAbility ability) {
        this(sourceCard, sourcePermanent, controllerId, zone, ability, null, null);
    }

    /** A candidate whose source is the face-up planar object {@code plane}. */
    public TriggerCandidate(Card sourceCard, Permanent sourcePermanent, UUID controllerId, Zone zone,
                            TriggeredAbility ability, PlanarObject plane) {
        this(sourceCard, sourcePermanent, controllerId, zone, ability, plane, null);
    }

    /** Whether the source is a planar object or an emblem, whose abilities function from the command zone. */
    public boolean commandZoneObject() {
        return plane != null || emblem != null;
    }

    /** The source as a permanent, for conditions that read one; a card in another zone gets a stand-in. */
    public Permanent sourceAsPermanent() {
        return sourcePermanent != null ? sourcePermanent : new Permanent(sourceCard);
    }

    /** The id the source's once-each-turn and once-only limits are kept under. */
    public UUID sourceObjectId() {
        return sourcePermanent != null ? sourcePermanent.getId() : sourceCard.getId();
    }
}
