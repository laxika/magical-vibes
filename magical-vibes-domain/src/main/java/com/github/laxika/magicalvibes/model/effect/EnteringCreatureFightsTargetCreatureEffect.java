package com.github.laxika.magicalvibes.model.effect;

/**
 * "That creature fights target creature" — the creature whose entry triggered the ability fights the
 * chosen target (CR 701.14a). The fighter is the stack entry's {@code triggeringPermanentId}, falling
 * back to the source permanent when the source itself is the creature that entered. Restrict the
 * target with the card's own {@code TargetFilter} (Gruul Ragebeast: a creature an opponent controls).
 */
public record EnteringCreatureFightsTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
