package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "307")
@CardRegistration(set = "4ED", collectorNumber = "254")
public class KillerBees extends Card {

    public KillerBees() {
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new BoostSelfEffect(1, 1)), "{G}: Killer Bees gets +1/+1 until end of turn."));
    }
}
