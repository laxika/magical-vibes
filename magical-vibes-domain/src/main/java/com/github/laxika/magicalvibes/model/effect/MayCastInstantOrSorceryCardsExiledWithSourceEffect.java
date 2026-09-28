package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers the controller one instant or sorcery card exiled with the source permanent to cast
 * during the resolving ability.
 */
public record MayCastInstantOrSorceryCardsExiledWithSourceEffect(
        boolean withoutPayingManaCost,
        boolean anyManaType,
        boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard)
        implements CardEffect {

    public MayCastInstantOrSorceryCardsExiledWithSourceEffect() {
        this(false, false, true);
    }

    public MayCastInstantOrSorceryCardsExiledWithSourceEffect(boolean withoutPayingManaCost) {
        this(withoutPayingManaCost, false, true);
    }
}
