package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "48")
public class ThrabenStandardBearer extends Card {

    public ThrabenStandardBearer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                1,
                                "Human Soldier",
                                1,
                                1,
                                CardColor.WHITE,
                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                Set.of(),
                                Set.of()
                        )
                ),
                "{1}{W}, {T}, Discard a card: Create a 1/1 white Human Soldier creature token."
        ));
    }
}
