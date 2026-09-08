package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player reveals a card at random from their hand. If it is an instant or sorcery,
 * exile it and let the effect controller cast it without paying its mana cost until the next end
 * step; if it is not cast, return it to its owner's hand at that end step.
 */
public record ExileRandomInstantOrSorceryFromTargetHandMayCastFreeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
