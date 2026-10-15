package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrPutCounterIfTreasureManaEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "194")
public class JetmirsFixer extends Card {

    public JetmirsFixer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{G}",
                List.of(new BoostSelfOrPutCounterIfTreasureManaEffect()),
                "{R}{G}: This creature gets +1/+1 until end of turn. If mana from a Treasure was spent to activate this ability, put a +1/+1 counter on this creature instead."
        ));
    }
}
