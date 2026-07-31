package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "104a")
@CardRegistration(set = "ALL", collectorNumber = "104b")
public class YavimayaAncients extends Card {

    public YavimayaAncients() {
        // {G}: This creature gets +1/-2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new BoostSelfEffect(1, -2)),
                "{G}: Yavimaya Ancients gets +1/-2 until end of turn."
        ));
    }
}
