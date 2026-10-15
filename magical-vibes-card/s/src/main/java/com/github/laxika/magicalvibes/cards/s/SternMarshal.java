package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "POR", collectorNumber = "32")
public class SternMarshal extends Card {

    public SternMarshal() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new BoostTargetCreatureEffect(2, 2)),
                "{T}: Target creature gets +2/+2 until end of turn. Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
