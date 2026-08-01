package com.github.laxika.magicalvibes.model.effect;

/**
 * "Enchanted creature phases out." (Vanishing). Non-targeting Aura activated ability: resolution
 * finds the Aura via the stack entry's {@code sourcePermanentId}, then phases out the permanent it
 * is attached to. Attachments (including the Aura) follow indirectly (CR 702.26g); the creature
 * phases in during its controller's next untap step (CR 702.26a).
 *
 * <p>Enchanted sibling of {@link PhaseOutSelfEffect} / {@link PhaseOutTargetPermanentEffect}.
 */
public record PhaseOutEnchantedCreatureEffect() implements CardEffect {
}
