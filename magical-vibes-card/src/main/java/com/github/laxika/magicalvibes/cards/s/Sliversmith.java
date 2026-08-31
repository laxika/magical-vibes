package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "163")
public class Sliversmith extends Card {

    public Sliversmith() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                "Metallic Sliver", 1, 1, null,
                                List.of(CardSubtype.SLIVER), Set.of(), Set.of(CardType.ARTIFACT)
                        )
                ),
                "{1}, {T}, Discard a card: Create a 1/1 colorless Sliver artifact creature token named Metallic Sliver."
        ));
    }
}
