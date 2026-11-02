package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "114")
public class ShockmawDragon extends Card {

    public ShockmawDragon() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToEachCreatureDamagedPlayerControlsEffect(1));
    }
}
