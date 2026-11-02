package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "162")
@CardRegistration(set = "A25", collectorNumber = "156")
@CardRegistration(set = "MUL", collectorNumber = "25")
@CardRegistration(set = "MUL", collectorNumber = "90")
@CardRegistration(set = "MUL", collectorNumber = "155")
@CardRegistration(set = "CMM", collectorNumber = "268")
@CardRegistration(set = "SLD", collectorNumber = "2406")
@CardRegistration(set = "SLD", collectorNumber = "2423")
public class ZadaHedronGrinder extends Card {

    public ZadaHedronGrinder() {
        addTrigger(Triggers.youCastSpell(),
                new CopySpellForEachOtherControlledCreatureEffect());
    }
}
