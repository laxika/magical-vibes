package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerAtLeastEffect;

@CardRegistration(set = "FEM", collectorNumber = "49a")
@CardRegistration(set = "FEM", collectorNumber = "49b")
@CardRegistration(set = "FEM", collectorNumber = "49c")
@CardRegistration(set = "FEM", collectorNumber = "49d")
public class BrassclawOrcs extends Card {

    public BrassclawOrcs() {
        addEffect(EffectSlot.STATIC, new CantBlockCreaturesWithPowerAtLeastEffect(2));
    }
}
