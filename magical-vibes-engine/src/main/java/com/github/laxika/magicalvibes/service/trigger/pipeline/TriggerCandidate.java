package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
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
 */
public record TriggerCandidate(Card sourceCard, Permanent sourcePermanent, UUID controllerId, Zone zone,
                               TriggeredAbility ability, PlanarObject plane) {

    /** A candidate whose source isn't a planar object. */
    public TriggerCandidate(Card sourceCard, Permanent sourcePermanent, UUID controllerId, Zone zone,
                            TriggeredAbility ability) {
        this(sourceCard, sourcePermanent, controllerId, zone, ability, null);
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
