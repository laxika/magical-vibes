package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the Auras that were attached to the creature that died from the controller's graveyard and reattaches the
 * Equipment that was, to the target creature. The attachments are the stack entry's triggering attachments, as they
 * were just before the creature left (CR 603.10a).
 */
public record ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
