package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Static self-effect: "this has all activated abilities of [cards]", mana abilities included (layer 6, CR 613.1f).
 * The cards are any static {@link CardSubject}: "all cards exiled with it" ({@link Subjects#exiledWithSource()}, by
 * one linked ability where the source can exile with more than one, CR 607.2a), "all creature cards in all
 * graveyards" ({@link Subjects#cardsInGraveyards}), "all cards you own in exile with cage counters on them"
 * ({@link Subjects#exiledCards(PlayerSubject, CardPredicate)}), "the top card of your library"
 * ({@link Subjects#topmostCardInYourLibrary}). A card exiled face down lends nothing (CR 406.3a), nor does a
 * graveyard card while graveyard cards have lost all abilities; a face-down source or one that lost its abilities
 * has none of them (CR 708.2).
 *
 * @param cards       whose abilities
 * @param filter      which of those cards lend them, judged as they are now ("as long as the top card of your library
 *                    is an artifact or creature card"); {@code null} for all of them
 * @param oncePerTurn "you may activate each of those abilities only once each turn"
 */
public record GainActivatedAbilitiesOfExiledCardsEffect(CardSubject cards, CardPredicate filter,
                                                       boolean oncePerTurn) implements CardEffect {

    public GainActivatedAbilitiesOfExiledCardsEffect {
        Objects.requireNonNull(cards, "cards");
    }

    /** "All activated abilities of all cards exiled with it". */
    public GainActivatedAbilitiesOfExiledCardsEffect() {
        this(Subjects.exiledWithSource(), null, false);
    }

    /** "All activated abilities of [cards]". */
    public GainActivatedAbilitiesOfExiledCardsEffect(CardSubject cards) {
        this(cards, null, false);
    }

    /** "Each activated ability of [cards]. You may activate each of those abilities only once each turn." */
    public GainActivatedAbilitiesOfExiledCardsEffect(CardSubject cards, boolean oncePerTurn) {
        this(cards, null, oncePerTurn);
    }
}
