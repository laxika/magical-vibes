package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Static: "You may [play lands and] cast spells from among [cards in exile]" for the ability's controller, whichever
 * permanent exiled them: Draugr Necromancer's "cards in exile your opponents own with ice counters on them"
 * ({@code exiledCards(eachOpponent(), allOf(nonland, CardHasCounterInExilePredicate(ICE)))}, nonland because it allows
 * only spells) and Grolnok, the Omnivore's "cards you own in exile with croak counters on them"
 * ({@code exiledCards(you(), CardHasCounterInExilePredicate(CROAK))}). A land card {@code cards} names may be played.
 * The permission is the source's static ability, so it stops while the source is face down or has lost its abilities
 * (CR 613.1f, 708.2).
 *
 * @param cards              which exiled cards: a static card subject, judged one card at a time
 *                           ({@code SubjectResolutionService.namesExiledCard}); only {@code exiledCards(players, filter)}
 * @param snowManaAsAnyColor "you may spend mana from snow sources as though it were mana of any color to cast those
 *                           spells"
 */
public record PlayFromAmongExiledCardsEffect(CardSubject cards, boolean snowManaAsAnyColor) implements CardEffect {

    public PlayFromAmongExiledCardsEffect {
        Objects.requireNonNull(cards, "cards");
        if (!(cards instanceof CardSubject.InExile)) {
            throw new IllegalArgumentException("A static permission names exiled cards by zone: " + cards);
        }
    }

    /** "You may play lands and cast spells from among [cards]". */
    public PlayFromAmongExiledCardsEffect(CardSubject cards) {
        this(cards, false);
    }
}
