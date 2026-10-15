package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "163")
public class StalkingBloodsucker extends Card {

    public StalkingBloodsucker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new DiscardCardTypeCost(null, null), new BoostSelfEffect(2, 2)),
                "{1}{B}, Discard a card: This creature gets +2/+2 until end of turn."));
    }
}
