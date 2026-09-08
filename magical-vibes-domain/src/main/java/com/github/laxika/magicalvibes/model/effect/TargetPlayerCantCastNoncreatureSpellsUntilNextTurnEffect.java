package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player can't cast noncreature spells until the activating player's next turn.
 */
public record TargetPlayerCantCastNoncreatureSpellsUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
