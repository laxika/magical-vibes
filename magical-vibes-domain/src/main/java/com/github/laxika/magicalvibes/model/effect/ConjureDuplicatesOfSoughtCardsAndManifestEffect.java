package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Conjures duplicates of the cards from a seek event, then manifests those duplicates. */
public record ConjureDuplicatesOfSoughtCardsAndManifestEffect(List<Card> soughtCards)
        implements CardEffect {

    public ConjureDuplicatesOfSoughtCardsAndManifestEffect() {
        this(List.of());
    }

    public ConjureDuplicatesOfSoughtCardsAndManifestEffect {
        soughtCards = List.copyOf(soughtCards);
    }
}
