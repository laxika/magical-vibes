package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "23")
public class ShuFarmer extends Card {

    public ShuFarmer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new GainLifeEffect(1)),
                "{T}: You gain 1 life. Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
