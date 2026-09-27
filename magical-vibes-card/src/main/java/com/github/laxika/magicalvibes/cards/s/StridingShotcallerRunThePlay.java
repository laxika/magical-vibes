package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RunThePlay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Striding Shotcaller // Run the Play (SOC 50). */
@CardRegistration(set = "SOC", collectorNumber = "50")
@CardRegistration(set = "SOC", collectorNumber = "98")
public class StridingShotcallerRunThePlay extends Card {

    public StridingShotcallerRunThePlay() {
        setBackFaceCard(new RunThePlay());

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new BecomePreparedEffect(), false, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "RunThePlay";
    }
}
