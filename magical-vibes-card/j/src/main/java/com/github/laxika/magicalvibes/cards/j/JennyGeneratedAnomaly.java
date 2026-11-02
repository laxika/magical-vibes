package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "137")
@CardRegistration(set = "WHO", collectorNumber = "421")
@CardRegistration(set = "WHO", collectorNumber = "742")
@CardRegistration(set = "WHO", collectorNumber = "1012")
public class JennyGeneratedAnomaly extends Card {

    public JennyGeneratedAnomaly() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExploreEffect());
    }
}
