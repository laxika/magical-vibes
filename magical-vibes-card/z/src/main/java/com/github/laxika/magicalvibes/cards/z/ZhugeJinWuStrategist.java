package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "66")
public class ZhugeJinWuStrategist extends Card {

    public ZhugeJinWuStrategist() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new MakeCreatureUnblockableEffect()),
                "{T}: Target creature can't be blocked this turn. Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
