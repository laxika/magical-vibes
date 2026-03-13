package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "225")
public class GargoyleCastle extends Card {

    public GargoyleCastle() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {5}, {T}, Sacrifice this land: Create a 3/4 colorless Gargoyle artifact creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateCreatureTokenEffect(
                                "Gargoyle", 3, 4, null,
                                List.of(CardSubtype.GARGOYLE),
                                Set.of(Keyword.FLYING),
                                Set.of(CardType.ARTIFACT)
                        )
                ),
                "{5}, {T}, Sacrifice this land: Create a 3/4 colorless Gargoyle artifact creature token with flying."
        ));
    }
}
