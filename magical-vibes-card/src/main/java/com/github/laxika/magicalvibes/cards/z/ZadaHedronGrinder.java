package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;

@CardRegistration(set = "BFZ", collectorNumber = "162")
@CardRegistration(set = "A25", collectorNumber = "156")
@CardRegistration(set = "MUL", collectorNumber = "25")
@CardRegistration(set = "MUL", collectorNumber = "90")
@CardRegistration(set = "MUL", collectorNumber = "155")
@CardRegistration(set = "CMM", collectorNumber = "268")
public class ZadaHedronGrinder extends Card {

    public ZadaHedronGrinder() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopySpellForEachOtherControlledCreatureEffect());
    }
}
