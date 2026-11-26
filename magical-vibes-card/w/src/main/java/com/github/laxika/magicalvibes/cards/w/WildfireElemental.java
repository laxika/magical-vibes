package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "297")
public class WildfireElemental extends Card {

    public WildfireElemental() {
        addTrigger(Triggers.opponentDealtNoncombatDamage(), BoostEffect.creaturesYouControl(1, 0));
    }
}
