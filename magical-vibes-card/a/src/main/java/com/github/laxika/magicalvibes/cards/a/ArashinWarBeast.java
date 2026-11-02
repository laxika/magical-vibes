package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "123")
public class ArashinWarBeast extends Card {

    public ArashinWarBeast() {
        addTrigger(Triggers.dealsCombatDamageToBlockingCreature(), new ManifestTopCardEffect());
    }
}
