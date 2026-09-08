package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Creates a copy of Spellweaver Volute's enchanted instant and offers to cast it for free. */
public record CopyEnchantedInstantAndMayCastCopyEffect(UUID enchantedCardId) implements CardEffect {
}
