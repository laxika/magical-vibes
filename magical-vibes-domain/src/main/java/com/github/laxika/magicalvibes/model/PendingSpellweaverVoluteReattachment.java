package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** State retained while a Spellweaver Volute copy is being cast and its Aura is reattached. */
public record PendingSpellweaverVoluteReattachment(
        UUID copyCardId,
        UUID auraPermanentId,
        UUID enchantedCardId,
        UUID controllerId) {
}
