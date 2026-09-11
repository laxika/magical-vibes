package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "303")
public class LibraryOfAlexandria extends Card {

    public LibraryOfAlexandria() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Draw a card. Activate only if you have exactly seven cards in hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1)),
                "{T}: Draw a card. Activate only if you have exactly seven cards in hand."
        ).withMinCardsInHand(7).withMaxCardsInHand(7));
    }
}
