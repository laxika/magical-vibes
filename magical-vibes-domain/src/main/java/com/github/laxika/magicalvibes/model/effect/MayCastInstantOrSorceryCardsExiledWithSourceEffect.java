package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers the controller one instant or sorcery card exiled with the source permanent to cast
 * during the resolving ability. The card is put on the bottom of its owner's library instead of
 * into a graveyard only for the normal-cost mode.
 */
public record MayCastInstantOrSorceryCardsExiledWithSourceEffect(boolean withoutPayingManaCost)
        implements CardEffect {

    public MayCastInstantOrSorceryCardsExiledWithSourceEffect() {
        this(false);
    }
}
