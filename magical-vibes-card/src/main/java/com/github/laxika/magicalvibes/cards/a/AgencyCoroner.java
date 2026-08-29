package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfSacrificedCreatureWasSuspectedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "75")
public class AgencyCoroner extends Card {

    public AgencyCoroner() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true, null, true),
                        new DrawCardEffect(1),
                        new DrawCardIfSacrificedCreatureWasSuspectedEffect()
                ),
                "{2}{B}, Sacrifice another creature: Draw a card. If the sacrificed creature was suspected, draw two cards instead."
        ));
    }
}
