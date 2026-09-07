package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "22")
public class GoldmeadowLookout extends Card {

    public GoldmeadowLookout() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Goldmeadow Harrier",
                                1,
                                1,
                                CardColor.WHITE,
                                null,
                                List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER),
                                Set.of(),
                                Set.of(),
                                false,
                                false,
                                Map.of(),
                                List.of(new ActivatedAbility(
                                        true,
                                        "{W}",
                                        List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                                        "{W}, {T}: Tap target creature.",
                                        TargetFilters.creature()
                                )),
                                false,
                                false,
                                false,
                                0,
                                Set.of()
                        )
                ),
                "{W}, {T}, Discard a card: Create a 1/1 white Kithkin Soldier creature token named Goldmeadow Harrier. It has \"{W}, {T}: Tap target creature.\""
        ));
    }
}
