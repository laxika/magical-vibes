package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the controller's top library card, makes the target opponent lose life equal to its
 * mana value, then offers the card for a free cast if it is an instant or sorcery.
 */
public record ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
