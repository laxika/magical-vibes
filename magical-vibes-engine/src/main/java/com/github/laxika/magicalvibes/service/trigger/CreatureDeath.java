package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;

import java.util.UUID;

/**
 * A permanent that died, as a "whenever [an object] dies" ability reads it: as it last existed on the battlefield
 * (CR 603.10a).
 *
 * @param permanent    its last-known copy, whose card holds the characteristics it died with
 * @param card         the physical card that went to the graveyard
 * @param ownerId      its owner
 * @param controllerId who controlled it as it died
 * @param power        its last-known power
 * @param toughness    its last-known toughness
 * @param attachments  the Auras and Equipment attached to it as it died
 */
public record CreatureDeath(Permanent permanent, Card card, UUID ownerId, UUID controllerId, int power, int toughness,
                            DepartedAttachments attachments) {

    /** The death a departure {@code event} to a graveyard records. */
    public static CreatureDeath of(ZoneChangeEvent event) {
        Permanent lastKnown = event.permanent();
        int power = lastKnown.getLastKnownPower() != null ? lastKnown.getLastKnownPower()
                : lastKnown.getEffectivePower();
        int toughness = lastKnown.getLastKnownToughness() != null ? lastKnown.getLastKnownToughness()
                : lastKnown.getEffectiveToughness();
        return new CreatureDeath(lastKnown, event.card(), event.ownerId(), event.controllerId(), power, toughness,
                event.attachments());
    }

    /** The card it died as: for a copy, the card it copied, with the types it had. */
    public Card dyingCard() {
        return permanent.getCard();
    }

    public UUID permanentId() {
        return permanent.getId();
    }
}
