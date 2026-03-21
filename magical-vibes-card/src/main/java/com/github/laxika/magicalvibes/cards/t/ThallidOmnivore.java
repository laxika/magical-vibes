package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "106")
public class ThallidOmnivore extends Card {

    public ThallidOmnivore() {
        // {1}, Sacrifice a Saproling: Thallid Omnivore gets +2/+2 until end of turn. You gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSubtypeCreatureCost(CardSubtype.SAPROLING),
                        new BoostSelfEffect(2, 2),
                        new GainLifeEffect(2)
                ),
                "{1}, Sacrifice a Saproling: Thallid Omnivore gets +2/+2 until end of turn. You gain 2 life."
        ));

        // {1}, Sacrifice another creature: Thallid Omnivore gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new BoostSelfEffect(2, 2)
                ),
                "{1}, Sacrifice another creature: Thallid Omnivore gets +2/+2 until end of turn."
        ));
    }
}
