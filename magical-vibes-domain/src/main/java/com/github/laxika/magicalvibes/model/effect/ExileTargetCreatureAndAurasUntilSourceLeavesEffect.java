package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles target creature and all Auras attached to it, tracking the cards with the source
 * permanent. The creature returns tapped with its counters restored when the source leaves or
 * becomes untapped, and the tracked Auras return attached when they can legally enchant it.
 */
public record ExileTargetCreatureAndAurasUntilSourceLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
