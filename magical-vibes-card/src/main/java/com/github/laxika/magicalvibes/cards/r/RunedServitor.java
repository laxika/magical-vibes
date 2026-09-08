package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "ORI", collectorNumber = "238")
@CardRegistration(set = "ROE", collectorNumber = "224")
public class RunedServitor extends Card {

    public RunedServitor() {
        // "When this creature dies, each player draws a card."
        addEffect(EffectSlot.ON_DEATH, new EachPlayerDrawsCardEffect(1));
    }
}
