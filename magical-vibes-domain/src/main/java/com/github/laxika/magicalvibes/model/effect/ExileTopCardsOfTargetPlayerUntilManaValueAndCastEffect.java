package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the top of a target player's library until a card with mana value one greater
 * than the spell exiled as this ability's cost is found, then offers that card for a free cast.
 */
public record ExileTopCardsOfTargetPlayerUntilManaValueAndCastEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
