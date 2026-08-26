package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Returns all cards exiled with the source permanent (tracked via
 * {@code GameData.exiledCards} by source permanent ID) to the battlefield. Used as a
 * death trigger by cards like Helvault whose abilities accumulate exiled cards and
 * release them when the source is put into a graveyard from the battlefield, and as a
 * sacrifice ability by Endless Sands and Cold Storage.
 *
 * @param underControllerControl when {@code true} the cards return under the ability
 *        controller's control ("under your control", Cold Storage); when {@code false}
 *        under their owners' control (Helvault, Endless Sands).
 * @param filter optional predicate restricting which exiled cards return; {@code null} returns all
 *        cards
 * @param turnFaceUp when {@code true}, turn every card exiled with the source face up before
 *        returning the matching cards (Pyxis of Pandemonium)
 * @param grantedKeywords keywords granted indefinitely to each card returned to the battlefield
 */
public record ReturnAllCardsExiledWithSourceEffect(boolean underControllerControl,
                                                   CardPredicate filter,
                                                   boolean turnFaceUp,
                                                   Set<Keyword> grantedKeywords) implements CardEffect {

    public ReturnAllCardsExiledWithSourceEffect() {
        this(false, null, false, Set.of());
    }

    public ReturnAllCardsExiledWithSourceEffect(boolean underControllerControl) {
        this(underControllerControl, null, false, Set.of());
    }

    public ReturnAllCardsExiledWithSourceEffect(CardPredicate filter) {
        this(false, filter, false, Set.of());
    }

    public ReturnAllCardsExiledWithSourceEffect(boolean underControllerControl,
                                                CardPredicate filter,
                                                boolean turnFaceUp) {
        this(underControllerControl, filter, turnFaceUp, Set.of());
    }
}
