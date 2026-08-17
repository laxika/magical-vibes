package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to you this turn, that damage is dealt
 * to target creature you control instead." The source is chosen on resolution, while the creature
 * is targeted when the ability is activated.
 */
public record RedirectNextDamageFromChosenSourceToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
