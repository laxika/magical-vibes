package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of Auras attached to the permanent the resolving trigger's event was about just before it left the
 * battlefield (CR 603.10a), the stack entry's triggering attachments: "for each Aura that was attached to it", or with
 * {@code controlledByYou} "for each Aura you controlled that was attached to it" (Hateful Eidolon), "you" the
 * ability's controller and "controlled" as the permanent left. 0 when the entry has no triggering permanent.
 *
 * @param controlledByYou count only the Auras the ability's controller controlled then
 */
public record AurasAttachedToTriggeringPermanent(boolean controlledByYou) implements DynamicAmount {

    /** Every Aura that was attached to it. */
    public AurasAttachedToTriggeringPermanent() {
        this(false);
    }
}
