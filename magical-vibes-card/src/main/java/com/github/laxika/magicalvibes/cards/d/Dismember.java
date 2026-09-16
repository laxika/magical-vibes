package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "NPH", collectorNumber = "57")
@CardRegistration(set = "MM2", collectorNumber = "79")
@CardRegistration(set = "MD1", collectorNumber = "25")
@CardRegistration(set = "TSR", collectorNumber = "322")
@CardRegistration(set = "SPG", collectorNumber = "41")
@CardRegistration(set = "SOA", collectorNumber = "28")
public class Dismember extends Card {

    public Dismember() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-5, -5));
    }
}
