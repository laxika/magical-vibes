package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "103")
public class CallTheBloodline extends Card {

    public CallTheBloodline() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(1, "Vampire Knight", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.VAMPIRE, CardSubtype.KNIGHT),
                                Set.of(Keyword.LIFELINK), Set.of())
                ),
                "{1}, Discard a card: Create a 1/1 black Vampire Knight creature token with lifelink. Activate only once each turn.",
                1
        ));
    }
}
