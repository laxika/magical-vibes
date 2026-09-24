package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "161")
@CardRegistration(set = "CMM", collectorNumber = "512")
@CardRegistration(set = "C14", collectorNumber = "23")
public class GhoulcallerGisa extends Card {

    public GhoulcallerGisa() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new SacrificeCreatureCost(false, true, false, true),
                        new CreateTokenEffect(
                                new XValue(), "Zombie", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())
                ),
                "{B}, {T}, Sacrifice another creature: Create X 2/2 black Zombie creature tokens, "
                        + "where X is the sacrificed creature's power."
        ));
    }
}
