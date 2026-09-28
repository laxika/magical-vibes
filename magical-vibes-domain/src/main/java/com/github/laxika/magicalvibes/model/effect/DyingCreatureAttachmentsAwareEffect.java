package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Binds attachment identities captured when a creature-death trigger is collected. */
public interface DyingCreatureAttachmentsAwareEffect {

    CardEffect boundToDyingCreatureAttachments(List<UUID> auraCardIds,
                                                List<UUID> equipmentPermanentIds);
}
