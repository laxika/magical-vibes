package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "211")
public class PackGuardian extends Card {

    public PackGuardian() {
        // When this creature enters, you may discard a land card. If you do, create a 2/2 green
        // Wolf creature token.
        // (Flash is loaded from Scryfall.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardThenEffect(
                        new CardTypePredicate(CardType.LAND),
                        new CreateTokenEffect("Wolf", 2, 2,
                                CardColor.GREEN, List.of(CardSubtype.WOLF),
                                Set.of(), Set.of()),
                        "a land card"),
                "Discard a land card to create a 2/2 green Wolf token?"
        ));
    }
}
