package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "76")
public class KheruDreadmaw extends Card {

    public KheruDreadmaw() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificeCreatureCost(false, false, true, true),
                        new GainLifeEffect(new XValue())
                ),
                "{1}{G}, Sacrifice another creature: You gain life equal to the sacrificed creature's toughness."
        ));
    }
}
