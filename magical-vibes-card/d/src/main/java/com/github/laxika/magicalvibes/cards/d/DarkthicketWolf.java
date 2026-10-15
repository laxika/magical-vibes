package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "175")
public class DarkthicketWolf extends Card {

    public DarkthicketWolf() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new BoostSelfEffect(2, 2)),
                "{2}{G}: Darkthicket Wolf gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
