package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.NoncombatDamageToOpponentCreaturesAsMinusCountersEffect;


@CardRegistration(set = "AKH", collectorNumber = "148")
@CardRegistration(set = "AKR", collectorNumber = "175")
@CardRegistration(set = "SLD", collectorNumber = "190")
@CardRegistration(set = "SLD", collectorNumber = "2039")
@CardRegistration(set = "SLD", collectorNumber = "2044")
public class SoulScarMage extends Card {

    public SoulScarMage() {
        // If a source you control would deal noncombat damage to a creature an opponent controls,
        // put that many -1/-1 counters on that creature instead.
        addStaticEffect(new NoncombatDamageToOpponentCreaturesAsMinusCountersEffect());
    }
}
