package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect;

@CardRegistration(set = "LEG", collectorNumber = "49")
public class BrineHag extends Card {

    public BrineHag() {
        addEffect(EffectSlot.ON_DEATH,
                new SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect(0, 2));
    }
}
