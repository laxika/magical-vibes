package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeIfSacrificedCardMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "106")
public class ThallidOmnivore extends Card {

    public ThallidOmnivore() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new BoostSelfEffect(2, 2),
                        new GainLifeIfSacrificedCardMatchesEffect(new CardSubtypePredicate(CardSubtype.SAPROLING), 2)
                ),
                "{1}, Sacrifice another creature: This creature gets +2/+2 until end of turn. If a Saproling was sacrificed this way, you gain 2 life."
        ));
    }
}
