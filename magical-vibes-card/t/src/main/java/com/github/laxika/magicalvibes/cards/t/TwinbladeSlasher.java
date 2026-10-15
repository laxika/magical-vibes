package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "79")
public class TwinbladeSlasher extends Card {

    public TwinbladeSlasher() {
        // {1}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.
        // (Wither is a keyword auto-loaded from Scryfall.)
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new BoostSelfEffect(2, 2)),
                "{1}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.", 1));
    }
}
