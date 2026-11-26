package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "P02", collectorNumber = "5")
public class AlabornVeteran extends Card {

    public AlabornVeteran() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(BoostEffect.target(2, 2)),
                "{T}: Target creature gets +2/+2 until end of turn. Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
